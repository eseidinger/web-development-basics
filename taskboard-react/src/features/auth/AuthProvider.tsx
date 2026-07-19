import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react'
import Keycloak, { type KeycloakProfile } from 'keycloak-js'
import { configureApiClient } from '../../lib/api-client'
import { loadPublicConfig, type PublicConfig } from '../../lib/public-config'

type AuthState = {
  isReady: boolean
  isAuthenticated: boolean
  config: PublicConfig | undefined
  error: string | undefined
  username: string | undefined
  email: string | undefined
  login: () => Promise<void>
  logout: () => Promise<void>
  getAccessToken: () => Promise<string | undefined>
}

const AuthContext = createContext<AuthState | undefined>(undefined)

function clearKeycloakErrorHash() {
  const hash = window.location.hash
  if (!hash.includes('error=') || !hash.includes('state=')) {
    return
  }

  window.history.replaceState({}, document.title, window.location.pathname + window.location.search)
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [isReady, setIsReady] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [config, setConfig] = useState<PublicConfig>()
  const [error, setError] = useState<string>()
  const [profile, setProfile] = useState<KeycloakProfile>()
  const keycloakRef = useRef<Keycloak | null>(null)

  const refreshProfile = useCallback(async () => {
    const keycloak = keycloakRef.current
    if (!keycloak?.authenticated) {
      setProfile(undefined)
      return
    }

    const loadedProfile = await keycloak.loadUserProfile()
    setProfile(loadedProfile)
  }, [])

  useEffect(() => {
    let active = true

    async function initialize() {
      try {
        clearKeycloakErrorHash()

        const publicConfig = await loadPublicConfig()
        const keycloak = new Keycloak({
          url: publicConfig.keycloakUrl,
          realm: publicConfig.keycloakRealm,
          clientId: publicConfig.keycloakClientId,
        })

        keycloakRef.current = keycloak
        configureApiClient(async () => {
          if (!keycloak.authenticated) {
            return undefined
          }

          await keycloak.updateToken(30)
          return keycloak.token
        })

        keycloak.onAuthSuccess = () => {
          setIsAuthenticated(true)
          setError(undefined)
          void refreshProfile()
        }
        keycloak.onAuthLogout = () => {
          setIsAuthenticated(false)
          setProfile(undefined)
        }
        keycloak.onTokenExpired = () => {
          void keycloak.updateToken(30)
        }

        const authenticated = await keycloak.init({
          onLoad: 'check-sso',
          pkceMethod: 'S256',
          checkLoginIframe: false,
          silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
          silentCheckSsoFallback: false,
        })

        if (!active) {
          return
        }

        setConfig(publicConfig)
        setIsAuthenticated(authenticated)
        if (authenticated) {
          await refreshProfile()
        }
      } catch (cause) {
        if (!active) {
          return
        }

        setError(cause instanceof Error ? cause.message : 'Unable to initialize authentication.')
      }

      setIsReady(true)
    }

    void initialize()

    return () => {
      active = false
    }
  }, [refreshProfile])

  const value: AuthState = {
    isReady,
    isAuthenticated,
    config,
    error,
    username: profile?.username,
    email: profile?.email,
    login: async () => {
      await keycloakRef.current?.login()
    },
    logout: async () => {
      await keycloakRef.current?.logout({ redirectUri: window.location.origin })
    },
    getAccessToken: async () => {
      const keycloak = keycloakRef.current
      if (!keycloak?.authenticated) {
        return undefined
      }

      await keycloak.updateToken(30)
      return keycloak.token
    },
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
