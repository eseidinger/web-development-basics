function readEnv(name: string, fallback: string): string {
  const rawValue = import.meta.env[name]
  if (typeof rawValue !== 'string') {
    return fallback
  }

  const trimmed = rawValue.trim()
  return trimmed.length > 0 ? trimmed : fallback
}

export const appEnv = {
  apiUrl: readEnv('VITE_TASKBOARD_API_URL', 'http://localhost:8080'),
  keycloakUrl: readEnv('VITE_KEYCLOAK_URL', 'http://localhost:8090'),
  keycloakRealm: readEnv('VITE_KEYCLOAK_REALM', 'taskboard'),
  keycloakClientId: readEnv('VITE_KEYCLOAK_CLIENT_ID', 'taskboard-frontend'),
}
