# Azure Provider Configuration Guide for Admin Application

This guide details the steps to configure Azure AD App Registrations for the `ai-dial-admin` application environment.

---

## 1. Configure App Registrations

### a. Create the Client App Registration

**Name**: `Ai.Dial.Admin.SPA.TST`  
Used by the frontend application to authenticate via Azure AD.

Steps:

1. Go to **Azure Portal > App registrations** and create a new registration.
2. Name the app `Ai.Dial.Admin.SPA.TST`.
3. After creation, navigate to **Certificates & secrets**:
   - Create a **client secret**.
   - Note the value – it will be used in your frontend deployment as:
     - `AuthADminAzureAADClientSecret`
4. Navigate to the **Authentication** tab:
   - Add a **Web** platform.
   - Set the **redirect URI** to:
     ```
     https://<your_admin_dns>/api/auth/callback/azure-ad
     ```
![azure1.png](azure-provider/azure1.png)

---

### b. Create the API App Registration

**Name**: `Ai.Dial.Env.Tst`  
Used to expose APIs and assign user/group roles.

Steps:

1. Go to **Azure Portal > App registrations** and create a new registration.
2. Name the app `Ai.Dial.Env.Tst`.

#### i. Configure API Scope
- Navigate to **Expose an API** tab.
- Set the **Application ID URI** (e.g., `api://<app_client_id>`).
- Add a new **scope** for admin login:
  - Name: `admin`
  - Who can consent: Admins and Users
  - Description: Scope for admin access.
![azure2.png](azure-provider/azure2.png)
#### ii. Authorize Client Application
- Under **Expose an API**, add `Ai.Dial.Admin.SPA.TST`'s **client ID** as an **Authorized client application**.
![azure3.png](azure-provider/azure3.png)
#### iii. Add Custom App Role
- Go to **App roles** and add a new role:
  - Name: `ConfigAdmin`
  - Allowed member types: **Users/Groups**
  - Description: Application configuration administrator
![azure4.png](azure-provider/azure4.png)
#### iv. Configure Authentication
- Navigate to **Authentication** tab.
- Add a **Web** platform with the redirect URI https://<your_admin_dns>/
![azure5.png](azure-provider/azure5.png)
#### v. Token Configuration
- Go to the **Token Configuration** tab.
- Add **Group Claims**:
- Include groups in token (required for role assignments)
![azure6.png](azure-provider/azure6.png)
#### vi. Assign Users or Groups
- Go to **Enterprise Applications**.
- Find `Ai.Dial.Env.Tst`, open it.
- Under **Users and Groups**, assign users/groups with the **ConfigAdmin** role.

---

## Security Configuration

Configure the Azure identity provider using environment variables with the `providers.azure.*` prefix.

| Environment Variable Name | Value | Required | Description                                                                                 |
|--------------------------|-------|----------|---------------------------------------------------------------------------------------------|
| providers.azure.issuer | `<AZURE_TENANT_ID>` | Yes | Azure directory (tenant) ID. This is the issuer claim value in JWT tokens from Azure AD. |
| providers.azure.jwk-set-uri | https://login.microsoftonline.com/common/discovery/v2.0/keys | No | URI for JSON Web Key Set. Defaults to the common Azure AD endpoint if not specified. |
| providers.azure.aliases | login.microsoftonline.com, login.windows.net, ... | No | Aliases for accepted JWT token issuers (Azure-specific). Used to support multiple Azure cloud environments. |
| providers.azure.audiences | `<AZURE_CLIENT_ID>` | Yes | Unique identifier assigned to DIAL Admin backend application by Azure AD (Ai.Dial.Env.Tst Client ID). Can be a comma-separated list for multiple audiences. |
| providers.azure.role-claims | roles | No | JWT claim name for user roles. Defaults to "roles" if not specified. |
| providers.azure.allowed-roles | ConfigAdmin | No | Comma-separated list of roles with access permissions for this provider. If not specified, uses the default from `config.rest.security.default.allowedRoles` (ConfigAdmin,admin). |

**Note:** The `SECURITY_ALLOWED_ROLES` environment variable is no longer used. Use `providers.azure.allowed-roles` for provider-specific role configuration, or rely on the default `config.rest.security.default.allowedRoles` setting.

---

## Notes

- Replace `<your_admin_dns>` with your actual DNS.
- Ensure role assignments and group memberships propagate correctly.
- Validate authentication flows using Azure AD test users or service accounts.