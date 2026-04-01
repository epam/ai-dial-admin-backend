# README: Implementing Authorized S2S Communication with Auth0

This guide provides a step-by-step process to implement authorized server-to-server (S2S) communication between DIAL Core and DIAL Admin backend using Auth0 as the identity provider.

## Pre-requirements

Before proceeding with the implementation, ensure the following pre-requirements are met:

1. **DIAL Core Application Using Auth0 Identity Provider:**
   - The DIAL Core application should be configured to use Auth0 as the identity provider with admin roles.

2. **DIAL Core Admin Rules Configured:**
   - Auth0 M2M tokens include a standard `scope` claim containing the granted scopes as a space-separated string. Configure the DIAL Core admin rules to match against this claim:

     ```json
     "access.admin.rules": {
       "access": {
         "admin": {
           "rules": [
             {
               "function": "EQUAL",
               "source": "scope",
               "targets": ["admin"]
             }
           ]
         }
       }
     }
     ```

## Steps to Implement S2S Communication

### Step 1: Create a Machine-to-Machine Application in Auth0

1. In your Auth0 dashboard, navigate to **Applications → Applications** and create a new **Machine to Machine (M2M)** application for DIAL Admin.
2. During creation, authorize the application against the API that represents DIAL Core and grant it the required scopes (e.g., `admin`).
3. After creation, note the **Client ID** and **Client Secret** — these will be used in Step 2.

For detailed instructions, refer to the [Auth0 documentation on M2M applications](https://auth0.com/docs/get-started/auth0-overview/create-applications/machine-to-machine-apps).

### Step 2: Configure Helm Values for DIAL Admin Backend

1. Add the following variables to the Helm values for the DIAL Admin backend:

   ```yaml
   backend:
     env:
       ENABLE_CONFIG_AUTO_RELOAD: true
       CORE_AUTH_TOKEN_PROVIDER_URL: "https://<auth0_domain>/oauth"
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_ID: <m2m_app_client_id> # Created in Step 1
       CORE_AUTH_TOKEN_PROVIDER_SCOPE: <granted_scope> # e.g. admin
       CORE_AUTH_TOKEN_PROVIDER_AUDIENCE: <dial_core_api_audience> # Auth0 API identifier for DIAL Core
     secrets:
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_SECRET: <m2m_app_client_secret> # Created in Step 1
   ```

   - Replace `<auth0_domain>`, `<m2m_app_client_id>`, `<granted_scope>`, `<dial_core_api_audience>`, and `<m2m_app_client_secret>` with the appropriate values from your Auth0 tenant and application.

   > **Note:** `/token` will be appended automatically to `CORE_AUTH_TOKEN_PROVIDER_URL`. The URL must end with `/oauth` (e.g., `https://your-tenant.us.auth0.com/oauth`).

   > **Note:** `CORE_AUTH_TOKEN_PROVIDER_AUDIENCE` is required for Auth0 — it is sent as the `audience` field in the token request body so that Auth0 issues a token scoped to the correct API.

---

By following these steps, you will successfully implement authorized S2S communication between DIAL Core and DIAL Admin backend using Auth0. If you encounter any issues, please ensure all IDs, secrets, and audience values are correctly configured and that the necessary scopes are granted to the M2M application.
