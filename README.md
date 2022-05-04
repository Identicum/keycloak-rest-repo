# keycloak-rest-repo

Keycloak repo provider SPI to consume REST API.
Integrated with [Identicum rest-users-api](https://github.com/Identicum/rest-users-api)

All user management functions are implemented:
- List all users
- Search by username
- Create user
- Modiffy user
- Modify password
- Lock/unlock user
- Delete user

## Compile module
```sh
mvn clean package
```

## Run project
```sh
docker-compose up
```

## Test
- Navigate to http://localhost:8080/auth/realms/demorealm/account
- Select `Sign In`
- Register a new user
- Sign Out
- Sign In again, as the newly registered user

## How it works
<TODO>

## Realm configuration
A realm is automatically imported to simplify testing. This realm has the following configuration:
<TODO>

## Troubleshooting
- Keycloak log should detail module activity, configured in ./startup-scripts/custom.cli

- Login to the admin console at http://localhost:8080/auth/admin/
  - username: admin
  - password: admin
