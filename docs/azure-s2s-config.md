
# README: Implementing Authorized S2S Communication between DIAL Core and DIAL Admin Backend

This guide provides a step-by-step process to implement authorized server-to-server (S2S) communication between DIAL Core and DIAL Admin backend, enabling interaction with DIAL Core.

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
       CORE_AUTH_TOKEN_PROVIDER_URL: "https://login.microsoftonline.com/common/discovery/v2.0/keys"
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_ID: <app_registration_client_id> # Created in Step 1
       CORE_AUTH_TOKEN_PROVIDER_SCOPE: <dial_core_application_scope>
     secrets:
       CORE_AUTH_TOKEN_PROVIDER_CLIENT_SECRET: <app_registration_client_secret> # Created in Step 1
   ```

   - Replace `<app_registration_client_id>`, `<dial_core_application_scope>`, and `<app_registration_client_secret>` with the appropriate values obtained during the app registration process.
