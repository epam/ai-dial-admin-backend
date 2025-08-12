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


## Obtaining a Token from Keycloak

To easily obtain tokens from Keycloak for testing or troubleshooting, you can use the provided Postman collection: [`docs/sample/http-requests/Keycloak.postman_collection.json`](sample/http-requests/Keycloak.postman_collection.json).

**Follow these steps:**

1. **Configure Environment Variables**  
   Set the `KEYCLOAK_HOST` and `REALM` variables in your Postman environment to match your Keycloak instance.

2. **Initiate the Authorization Flow**  
   Open the `auth` request from the Postman collection in your browser. Complete the authentication process as prompted.

3. **Retrieve the Authorization Code**  
   After successful authentication, you will be redirected. Copy the `code` parameter from your browser's address bar.

4. **Set Up the Token Request**  
   - Paste the copied `code` value into the `AUTH_CODE` variable in the `token` request within Postman.
   - Set the `KEYCLOAK_SECRET_DIAL_ADMIN` variable to the client secret you obtained from the Keycloak admin console.

5. **Request the Token**  
   Execute the `token` request in Postman. You should receive an access token in the response.

This process will help you quickly obtain and test JWT tokens from your Keycloak setup.