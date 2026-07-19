import { useAuth } from '../auth/AuthProvider'
import { useBoardsQuery } from './api'

export function BoardListPanel() {
  const auth = useAuth()
  const boardsQuery = useBoardsQuery(auth.isAuthenticated)

  return (
    <section className="panel">
      <div className="panel__title-row">
        <div>
          <p className="panel__eyebrow">Server state</p>
          <h2>Boards query</h2>
        </div>
        <span className="status-pill">TanStack Query</span>
      </div>

      {!auth.isReady ? <p className="empty-state">Bootstrapping Keycloak session…</p> : null}

      {auth.isReady && !auth.isAuthenticated ? (
        <p className="empty-state">Sign in with Keycloak to fetch boards from the Spring API.</p>
      ) : null}

      {boardsQuery.isLoading ? <p className="empty-state">Loading boards…</p> : null}

      {boardsQuery.isError ? <p className="empty-state error">{boardsQuery.error.message}</p> : null}

      {boardsQuery.data && boardsQuery.data.length > 0 ? (
        <ul className="board-list">
          {boardsQuery.data.map((board) => (
            <li key={board.id}>
              <span>{board.name}</span>
              <small>{board.role}</small>
            </li>
          ))}
        </ul>
      ) : null}

      {boardsQuery.data && boardsQuery.data.length === 0 && auth.isAuthenticated ? (
        <p className="empty-state">No boards yet. Create one from the form panel.</p>
      ) : null}
    </section>
  )
}
