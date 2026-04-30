
<!-- omit from toc -->
# Configure Google Identity as Identity Provider

<div class="docusaurus-ignore">

<!-- omit from toc -->
## Table of Contents

- [Introduction](#introduction)
- [Configuration Guidelines](#configuration-guidelines)
  - [Configure Google Identity](#configure-google-identity)
  - [Configure AI DIAL](#configure-ai-dial)
    - [AI Dial Admin Frontend Settings](#ai-dial-admin-frontend-settings)
    - [AI Dial Admin Backend Settings](#ai-dial-admin-backend-settings)

</div>

## Introduction

This basic tutorial demonstrates how to configure [Google Identity](https://developers.google.com/identity/protocols/oauth2) and use it as an identity and access management solution for AI DIAL users.

In AI DIAL, you can assign roles to Models and Applications to restrict the number of tokens that can be transmitted in a specific time frame. These roles and their limitations can be created in external systems and then assigned in AI DIAL Admin's configuration.

## Configuration Guidelines

### Configure Google Identity

> **Note**: Replace `<frontend_url>` with the actual address of your AI Dial Admin Frontend application.

Follow these steps to configure Google Identity:

1. **Create an OAuth consent screen**: refer [Google documentation](https://developers.google.com/workspace/guides/configure-oauth-consent) to learn how to do this.
1. **Create Client ID and Secret**: in the **Google Auth Platform/Clients**, use **Create Client**:
    - **Application Type**: `Web Application`
    - **Name**, e.g. `ai-dial-admin`
    - **Authorized JavaScript origins**: `<frontend_url>`
    - **Authorized redirect URIs**: `https://<frontend_url>/api/auth/callback/google`
1. **Gather facts**: to proceed with DIAL configuration, collect information related to Google Identity from the modal window:
    - **Client ID** (`<google_client_id>`)
    - **Client secret** (`<google_client_secret>`)
1. (Optional, RBAC) **Create a Group and add members**: Once the application integration is set up, [create the necessary Group and add members in Google Group](https://support.google.com/a/answer/9400082?hl=en#zippy=%2Cstep-create-a-group).
1. (Optional, RBAC) **Enable the Google Cloud Identity API**: In **APIs & Services/Library**, [Search for `Cloud Identity API`](https://console.cloud.google.com/apis/api/cloudidentity.googleapis.com) and enable it.

### Configure AI DIAL ADMIN

By configuring both AI Dial Admin Frontend and AI Dial Admin Backend with the necessary environment variables, you will enable them to work together seamlessly with Identity Provider for authentication and authorization purposes.

#### AI Dial Admin Frontend  Settings

Add the following environment variables to AI Dial Admin Frontend [configuration](https://github.com/epam/ai-dial-admin-frontend?tab=readme-ov-file#environment-variables-for-the-configuration-of-auth-providers):

##### Using Access Token 
```yaml
AUTH_GOOGLE_CLIENT_ID: "<google_client_id>"
AUTH_GOOGLE_SECRET: "<google_client_secret>"
AUTH_GOOGLE_SCOPE: "openid email profile https://www.googleapis.com/auth/cloud-identity.groups.readonly"
```
##### Using Identity token 
```yaml
AUTH_GOOGLE_CLIENT_ID: "<google_client_id>"
AUTH_GOOGLE_SECRET: "<google_client_secret>"
AUTH_GOOGLE_SCOPE: "openid email profile https://www.googleapis.com/auth/cloud-identity.groups.readonly"
AUTH_IDTOKEN_PROVIDERS: "google"
```

#### AI Dial Admin Backend Settings

Add the following parameters to AI Dial Admin Backend [**static** settings](https://github.com/epam/ai-dial-core?tab=readme-ov-file#static-settings):

##### Using userinfo endpoint (working with access token from Ai Dial Admin Frontend)

```yaml
  providers.google.user-info-endpoint: "https://openidconnect.googleapis.com/v1/userinfo"
  providers.google.role-claims: "fn:getGoogleWorkspaceGroups"
  providers.google.principal-claim: "sub"
  providers.google.allowed-roles: "example@example.com"
  providers.google.email-claims: "email"
```
##### Using jwk-ser-uri (working with id token from Ai Dial Admin Frontend)

```yaml
  providers.google.jwk-set-uri: "https://www.googleapis.com/oauth2/v3/certs"
  providers.google.issuer: "https://accounts.google.com"
  providers.google.audiences: "<google_client_id>"
  providers.google.role-claims: "email"
  providers.google.allowed-roles: "example@epamle.com,example2@example.com" #list of emails 
  providers.google.email-claims: "email"

```
