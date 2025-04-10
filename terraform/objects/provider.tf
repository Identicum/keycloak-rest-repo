terraform {
  required_providers {
    keycloak = {
      source  = "registry.terraform.io/keycloak/keycloak"
      version = "~> 5.0.0"
    }
  }
}

provider "keycloak" {
  client_id     = "admin-cli"
  username      = "admin"
  password      = "admin"
  url           = "http://keycloak:8080"
}