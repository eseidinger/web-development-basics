export type PublicConfig = {
  apiBasePath: string
  keycloakUrl: string
  keycloakRealm: string
  keycloakClientId: string
}

export async function loadPublicConfig(): Promise<PublicConfig> {
  const response = await fetch('/api/config', {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Unable to load public configuration: ${response.status}`)
  }

  return (await response.json()) as PublicConfig
}