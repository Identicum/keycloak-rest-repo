resource "keycloak_custom_user_federation" "custom_user_federation" {
  name = "rest-repo-provider"
  realm_id = keycloak_realm.restusers.id
  provider_id = "rest-repo-provider"
  cache_policy = "NO_CACHE"
  priority = 0
  enabled = true
  config = {
    baseURL = "http://rest-users-api:8080/",
    authType = "NONE"
    maxHttpConnections: 5,
    apiSocketTimeout: 1000,
    apiConnectTimeout: 1000,
    apiConnectionRequestTimeout: 1000,
    httpStatsInterval: 3600,
  }
}
