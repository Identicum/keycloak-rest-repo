# keycloak-rest-repo

Plugin para consumo de identidades en Keycloak mediante API Rest

## Instalación

1. Empaquetado del jar: `mvn package`
2. Copiado del jar a la carpeta *$KEYCLOAK_HOME/standalone/deployments*
3. Configuración del módulo en Keycloak
    1. Ingresar a la consola como Admin
    2. Ir al menú de User Federation
    3. Agregar el provider
    4. Configurar el nombre y el baseURL (requerido)

En el ejemplo se encuentra integrado con la rest-users-api (https://github.com/Identicum/rest-users-api)

Implementadas las funciones `findByUsername`, `searchForUser` y `authenticate`.
