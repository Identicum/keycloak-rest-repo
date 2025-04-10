resource "keycloak_user" "user" {
  realm_id = keycloak_realm.restusers.id
  username = "myuser"
  enabled = true
  email = "user@id.com"
  first_name = "user"
  last_name = "user"

  initial_password {
    value = "123"
    temporary = true
  }
}