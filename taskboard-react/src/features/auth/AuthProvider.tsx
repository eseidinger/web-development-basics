import {
  createContext,
  type PropsWithChildren,
  useContext,
  useEffect,
  useState,
} from 'react'
import { configureApiClient } from '../../lib/api-client'
import { keycloak } from './keycloak'

type AuthState = {
  isReady: boolean
  isAuthenticated: boolean
  username: string | undefined
  email: string | undefined
  login: () => Promise<void>
  logout: () => Promise<void>
  getAccessToken: () => Promise<string | undefined>
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: PropsWithChildren) {
  const [isReady, setIsReady] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    let active = true

    async function initialize() {
      const authenticated = await keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      })

      if (!active) {
        return
      }

      setIsAuthenticated(authenticated)
      setIsReady(true)
    }

    keycloak.onAuthSuccess = () => setIsAuthenticated(true)
    keycloak.onAuthLogout = () => setIsAuthenticated(false)
    keycloak.onTokenExpired = () => {
      void keycloak.updateToken(30)
    }

    configureApiClient(async () => {
      if (!keycloak.authenticated) {
        return undefined
      }

      await keycloak.updateToken(30)
      return keycloak.token
    })

    void initialize()

    return () => {
      active = false
    }
  }, [])

  const value: AuthState = {
    isReady,
    isAuthenticated,
    username: keycloak.tokenParsed?.preferred_username as string | undefined,
    email: keycloak.tokenParsed?.email as string | undefined,
    login: async () => {
      await keycloak.login()
    },
    logout: async () => {
      await keycloak.logout({ redirectUri: window.location.origin })
    },
    getAccessToken: async () => {
      if (!keycloak.authenticated) {
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
