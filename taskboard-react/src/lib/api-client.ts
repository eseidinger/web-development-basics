import { appEnv } from './env'

export type AccessTokenProvider = () => Promise<string | undefined>

let accessTokenProvider: AccessTokenProvider = async () => undefined

export function configureApiClient(provider: AccessTokenProvider) {
  accessTokenProvider = provider
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const token = await accessTokenProvider()
  const headers = new Headers(init?.headers)
  headers.set('Accept', 'application/json')

  if (init?.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${appEnv.apiUrl}${path}`, {
    ...init,
    headers,
  })

  if (!response.ok) {
    const message = response.status === 401
      ? 'Authentication failed. Log in again to refresh the Keycloak session.'
      : `Request failed with status ${response.status}`
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
