# Keycloak configuration for local IDP integration
Run 
```shell
docker-compose up
```
to spin up Keycloak.

- Go to http://localhost:8888. Use default credentials (admin/admin) to log in.

- Go to Clients -> Create Client and start filling fields as follows.
![keycloak1.png](keycloak/keycloak1.png)
- Click "Next" and on "Capability config" page enable "Client Authentication" and click "Next".

- Fill URLs
![keycloak2.png](keycloak/keycloak2.png)
- And click "Save"

- Open newly created Client "cfg-mgmt" and go to "Roles" tab.
Create new role "ConfigAdmin".
- Now switch to "Credentials" tab and copy "Client Secret". It will be needed for IDP configuration in ai-dial-admin-backend.
- On the left pane go to "Users" and create new User.
  ![keycloak3.png](keycloak/keycloak3.png)
- Click create and go to "Credentials" tab. Set the password for that user and set "Temporary" flag to false
- Go to "Role Mapping" and click "Assign role". Search for "ConfgiAdmin" role created before. Assign selected role.
- Now go to ai-dial-admin-backend application.properties and configure Identity Provider as follows:
```properties
config.rest.security=oidc
com.c4-soft.springaddons.oidc.resourceserver.enabled=false
com.c4-soft.springaddons.oidc.ops[0].iss=http://localhost:8888/realms/master
com.c4-soft.springaddons.oidc.ops[0].authorities[0].path=$.resource_access.*.roles
com.c4-soft.springaddons.oidc.ops[0].username-claim=preferred_username
com.c4-soft.springaddons.oidc.ops[0].jwk-set-uri=http://localhost:8888/realms/master/protocol/openid-connect/certs
```
To use configured IDP with Sample HTTP Client located [here](sample/http-requests/AdminPanel.http) you need to set [env variables](sample/http-requests/http-client.env.json)
Replace `{client_secret}` with client secret that you copied from "Client Secret" tab
```json
{
  "dev": {
    "Security": {
      "Auth": {
        "keycloak": {
          "Type": "OAuth2",
          "Grant Type": "Authorization Code",
          "Client ID": "cfg-mgmt",
          "Redirect URL": "http://localhost:3000",
          "Auth URL": "http://localhost:8888/realms/master/protocol/openid-connect/auth",
          "Token URL": "http://localhost:8888/realms/master/protocol/openid-connect/token",
          "Client Secret":"{client_secret}"
        }
      }
    }
  }
}
```
Now you are ready to user Authorization based on JWT token. Well done!

