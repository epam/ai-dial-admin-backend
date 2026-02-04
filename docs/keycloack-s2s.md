# Keycloak S2S Communication Setup

This guide provides instructions for setting up Service-to-Service (S2S) communication between the DIAL Core application and the DIAL Admin backend using Keycloak as the identity provider.

## Prerequisites

- **DIAL Core App Client in Keycloak**: Ensure that the DIAL Core application is configured to use Keycloak as the identity provider with admin roles.
- **DIAL Core Admin Rules Configured**: The admin rules should be configured as follows:

  ```json
  "access.admin.rules": {
    "access": {
      "admin": {
        "rules": [
          {
            "function": "EQUAL",
            "source": "roles",
            "targets": ["admin"]
          }
        ]
      }
    }
  }
  ```

## Steps to Implement S2S Communication

### Step 1: Create a New Keycloak Client

Create a new Keycloak client, for example, `dial-admin-s2s`, with service account enabled:

```yaml
clients:
  - clientId: dial-admin-s2s
    name: dial-admin-s2s
    enabled: true
    clientAuthenticatorType: client-secret
    secret: $(env:KEYCLOAK_SECRET_S2S_AUTH)
    redirectUris: []
    webOrigins: []
    directAccessGrantsEnabled: false
    serviceAccountsEnabled: true
    authorizationServicesEnabled: false
    publicClient: false
    protocol: openid-connect
    attributes:
      "access.token.lifespan": "300" # Token lifespan in seconds default value may be skipped
    fullScopeAllowed: true #to have necessary  claim in access token
    nodeReRegistrationTimeout: -1
    defaultClientScopes:
      - roles
```

### Step 2: Assign Roles to Service Account User

Assign roles to the service account user :

```yaml
users:
  - username: service-account-dial-admin-s2s
    enabled: true
    serviceAccountClientId: dial-admin-s2s
    realmRoles:
      - admin
```

### Step 3: Configure Helm Values for DIAL Admin Backend

Add the following variables to the Helm values for the DIAL Admin backend:

```yaml
backend:
  env:
    ENABLE_CONFIG_AUTO_RELOAD: true
    CORE_AUTH_TOKEN_PROVIDER_URL: "https://<keycloak_host>/realms/<your_realm>/protocol/openid-connect"
    CORE_AUTH_TOKEN_PROVIDER_CLIENT_ID: <keycloak_s2s_client_id> # Created in Step 1
    CORE_AUTH_TOKEN_PROVIDER_SCOPE: <keycloak_scope> # For Keycloak, this can be empty
  secrets:
    CORE_AUTH_TOKEN_PROVIDER_CLIENT_SECRET: <keycloak_s2s_client_secret> # Created in Step 1
```

**Note**: `/token` will be added automatically to the end of the `CORE_AUTH_TOKEN_PROVIDER_URL` variable. Therefore, `CORE_AUTH_TOKEN_PROVIDER_URL` should not include this postfix.  

## Conclusion

By following these steps, you will successfully implement authorized S2S communication between the DIAL Core and DIAL Admin backend. If you encounter any issues, please ensure all IDs and secrets are correctly configured and that the necessary permissions are granted.

--- 

This README provides a structured approach to setting up S2S communication using Keycloak, ensuring that all necessary configurations and roles are correctly applied.
