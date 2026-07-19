import Keycloak from 'keycloak-js'
import { appEnv } from '../../lib/env'

export const keycloak = new Keycloak({
  url: appEnv.keycloakUrl,
  realm: appEnv.keycloakRealm,
  clientId: appEnv.keycloakClientId,
})
