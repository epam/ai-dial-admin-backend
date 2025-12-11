
# README: 

Here's the updated README guide with the pre-requirements section added:

---

# README: Implementing Authorized S2S Communication

This guide provides a step-by-step process to implement authorized server-to-server (S2S) communication between DIAL Core and DIAL Admin backend, enabling interaction with DIAL Core.

## Pre-requirements

Before proceeding with the implementation, ensure the following pre-requirements are met:

1. **DIAL Core App Registration with Roles:**
   - Ensure that the DIAL Core app registration includes roles ex. "ConfigAdmin, admin" role.

2. **DIAL Core Application Using Azure Identity Provider:**
   - The DIAL Core application should be configured to use Azure as the identity provider with admin roles.

3. **DIAL Core Admin Rules Configured:**
   - Ensure that the DIAL Core admin rules are configured as follows:

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

### Step 1: Create App Registration for DIAL Admin

1. Register a new application in your Azure Active Directory for DIAL Admin. Refer to the [Microsoft documentation on app registration](https://learn.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app) for detailed instructions.
2. Generate a client secret for this application. This will be used as the client for DIAL Admin. You can find more information on creating client secrets in the [Microsoft documentation](https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret).

### Step 2: Assign Necessary Rights

1. Use the following script to assign the necessary rights for the DIAL Admin app registration to the Core app registration:

   ```bash
   az rest -m post -u https://graph.microsoft.com/beta/servicePrincipals/<dial_admin_service_principle_object_id>/appRoleAssignments -b "{\"principalId\": \"<dial_admin_service_principle_object_id>\", \"resourceId\": \"<dial_core_service_principle_object_id>\",\"appRoleId\": \"<dial_core_role_id>\"}"
   ```

   - Replace `<dial_admin_service_principle_object_id>`, `<dial_core_service_principle_object_id>`, and `<dial_core_role_id>` with the appropriate values.
   - For more information on assigning app roles, refer to the [Microsoft documentation on app role assignments](https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps).

### Step 3: Configure Helm Values for DIAL Admin Backend

1. Add the following variables to the Helm values for the DIAL Admin backend:

   ```yaml
   backend:
     env:
       ENABLE_CONFIG_AUTO_RELOAD: true
       CORE_AUTH_TOKEN_PROVIDER_URL: "https://login.microsoftonline.com/<tenant_id>/oauth2/v2.0"
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_ID: <app_registration_client_id> # Created in Step 1
       CORE_AUTH_TOKEN_PROVIDER_SCOPE: <dial_core_application_scope>
     secrets:
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_SECRET: <app_registration_client_secret> # Created in Step 1
   ```

   - Replace `<app_registration_client_id>`, `<dial_core_application_scope>`, `<tenant_id>` and `<app_registration_client_secret>` with the appropriate values obtained during the app registration process.

---

By following these steps, you will successfully implement authorized S2S communication between DIAL Core and DIAL Admin backend. If you encounter any issues, please ensure all IDs and secrets are correctly configured and that the necessary permissions are granted.
