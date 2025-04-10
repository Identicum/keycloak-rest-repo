resource "keycloak_realm" "restusers" {
  realm   = "restusers"
  enabled = true

  registration_allowed           = true
  reset_password_allowed         = true
  edit_username_allowed          = false
  registration_email_as_username = false
} 