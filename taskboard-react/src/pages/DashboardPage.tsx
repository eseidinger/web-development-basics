import '../App.css'
import { useAuth } from '../features/auth/AuthProvider'
import { BoardCreatePanel } from '../features/boards/BoardCreatePanel'
import { BoardListPanel } from '../features/boards/BoardListPanel'
import { LaneBoardDemo } from '../features/lanes/LaneBoardDemo'

export function DashboardPage() {
  const auth = useAuth()

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <div className="hero-bar">
          <div>
            <p className="eyebrow">React comparison baseline</p>
            <h1>Task Board with real Keycloak-backed API calls.</h1>
          </div>

          <div className="auth-actions">
            {auth.isAuthenticated ? (
              <>
                <div className="auth-summary">
                  <strong>{auth.username ?? 'Authenticated user'}</strong>
                  <span>{auth.email ?? 'Keycloak session active'}</span>
                </div>
                <button type="button" className="ghost-button" onClick={() => void auth.logout()}>
                  Log out
                </button>
              </>
            ) : (
              <button type="button" className="ghost-button" onClick={() => void auth.login()}>
                Log in with Keycloak
              </button>
            )}
          </div>
        </div>

        <p className="hero-copy">
          The app now uses the lean comparison stack with Keycloak OIDC for auth, token-aware fetches for the Spring API,
          and separate feature modules for auth, boards, and lane interactions.
        </p>

        {auth.error ? <p className="empty-state error">{auth.error}</p> : null}

        {auth.config ? (
          <dl className="env-summary">
          <div>
            <dt>API</dt>
            <dd>{auth.config.apiBasePath}</dd>
          </div>
          <div>
            <dt>Realm</dt>
            <dd>{auth.config.keycloakRealm}</dd>
          </div>
          <div>
            <dt>Client</dt>
            <dd>{auth.config.keycloakClientId}</dd>
          </div>
          </dl>
        ) : null}
      </section>

      <section className="content-grid">
        <BoardListPanel />
        <BoardCreatePanel />
      </section>

      <LaneBoardDemo />
    </main>
  )
}
