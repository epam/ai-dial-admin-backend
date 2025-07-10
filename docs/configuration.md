# Configuration Guide

This document provides a comprehensive list of all configurable properties in the AIDIAL Admin Panel Backend.

## Table of Contents

- [AIDIAL Config File Exporter](#aidial-config-file-exporter)
- [Kubernetes Configuration](#kubernetes-configuration)
- [Web Server Configuration](#web-server-configuration)
- [Security Configuration](#security-configuration)
- [Cloud Provider Configuration](#cloud-provider-configuration)
- [DIAL Core Configuration](#dial-core-configuration)
- [OpenTelemetry Configuration](#opentelemetry-configuration)
- [Actuator Configuration](#actuator-configuration)
- [Datasource Configuration](#datasource-configuration)
- [Metrics Configuration](#metrics-configuration)
- [Logging Configuration](#logging-configuration)
- [Retry Configuration](#retry-configuration)

## AIDIAL Config File Exporter

| Setting                                  | Environment Variable                     | Default              | Description                                                                                                                                 |
|------------------------------------------|------------------------------------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| config.export.enabled                    | CONFIG_EXPORT_ENABLED                    | true                 | Enables or disables DIAL Core configuration file export functionality                                                                       |
| config.version.target                    | CORE_CONFIG_VERSION                      | latest               | Version of DIAL Core configuration used in file export functionality                                                                        |
| config.export.syncPeriod                 | CONFIG_EXPORT_SYNCPERIOD                 | 15000                | Interval in milliseconds for DIAL configuration export                                                                                      |
| config.export.delayConfigReload          | DELAY_CONFIG_RELOAD_MILLISECONDS         | 5000                 | Delay in milliseconds before calling the /reload_config endpoint on core after writing to destination storage (e.g., configMap sync period) |
| config.export.storageType                | CONFIG_EXPORT_STORAGETYPE                | LOCAL_FILE           | Type of storage for DIAL configuration export (KUBE_SECRET, CONFIG_MAP, LOCAL_FILE)                                                         |
| config.export.outputFile.path            | CONFIG_EXPORT_OUTPUTFILE_PATH            | data/export/out.json | Path for configuration file when using LOCAL_FILE storage type                                                                              |
| config.export.configMap.names            | CONFIG_EXPORT_CONFIGMAP_NAMES            | core-config-git      | Comma separated names of the ConfigMaps used for DIAL configuration export                                                                  |
| config.export.configMap.key              | CONFIG_EXPORT_CONFIGMAP_KEY              | env.config.json      | Key in ConfigMap used for DIAL configuration export                                                                                         |                                                                          
| config.export.kubeSecret.names           | CONFIG_EXPORT_KUBESECRET_NAMES           | kube-secret-name       | Comma separated names of the Kubernetes Secrets used for DIAL configuration export                                                          |
| config.export.kubeSecret.key             | CONFIG_EXPORT_KUBESECRET_KEY             | kube-secret-key        | Key in Kubernetes Secret used for DIAL configuration export                                                                                 |
| config.export.keyvault.type              | CONFIG_EXPORT_KEYVAULT_TYPE              | none                 | Type of keyvault storage for secret values (none, azure, vault, aws, gcp)                                                                   |
| config.export.keyvault.secretNames       | CONFIG_EXPORT_KEYVAULT_SECRETNAMES       | -                    | Names of secrets in keyvault (used when keyvault.type is azure, vault, or aws)                                                              |
| config.export.keyvault.secretPath        | CONFIG_EXPORT_KEYVAULT_SECRETPATH        | -                    | Path to secrets in keyvault (used when keyvault.type is vault)                                                                              |
| config.export.keyvault.expiration.period | CONFIG_EXPORT_KEYVAULT_EXPIRATION_PERIOD | 3                    | Expiration period for keyvault values                                                                                                       |
| config.export.keyvault.expiration.unit   | CONFIG_EXPORT_KEYVAULT_EXPIRATION_UNIT   | MONTHS               | Unit of time for keyvault value expiration                                                                                                  |
| config.export.createResources            | CONFIG_EXPORT_CREATE_RESOURCES           | false                | If true, create resources where config is exported if they don't already exist                                                              |

## Kubernetes Configuration
config.export.storageType=CONFIG_MAP|KUBE_SECRET

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| kubernetes-config.connectType | KUBERNETES_CONFIG_CONNECTTYPE | CONFIG_FILE | Kubernetes connection type (CONFIG_FILE or TOKEN) |
| kubernetes-config.masterUrl | KUBERNETES_CONFIG_MASTERURL | url | Kubernetes master URL (required when connectType is TOKEN) |
| kubernetes-config.oauthToken | KUBERNETES_CONFIG_OAUTHTOKEN | token | Kubernetes OAuth token (required when connectType is TOKEN) |
| kubernetes-config.trustCerts | KUBERNETES_CONFIG_TRUSTCERTS | false | Trust all Kubernetes certificates |
| kubernetes-config.namespace | KUBERNETES_CONFIG_NAMESPACE | default | Kubernetes namespace for operations |
| kubernetes-config.client.maxConcurrentRequests | KUBERNETES_CONFIG_CLIENT_MAXCONCURRENTREQUESTS | 64 | Maximum number of concurrent requests to Kubernetes API |
| kubernetes-config.client.maxConcurrentRequestsPerHost | KUBERNETES_CONFIG_CLIENT_MAXCONCURRENTREQUESTSPERHOST | 64 | Maximum number of concurrent requests per Kubernetes host |
| kubernetes-config.client.requestRetryBackoffLimit | KUBERNETES_CONFIG_CLIENT_REQUESTRETRYBACKOFFLIMIT | 10 | Maximum number of retry attempts for failed requests |
| kubernetes-config.client.requestTimeout | KUBERNETES_CONFIG_CLIENT_REQUESTTIMEOUT | 20000 | Request timeout in milliseconds |
| kubernetes-config.client.withWebsocketPingInterval | KUBERNETES_CONFIG_CLIENT_WITHWEBSOCKETPINGINTERVAL | 120000 | WebSocket ping interval in milliseconds |
| kubernetes-config.client.withWatchReconnectLimit | KUBERNETES_CONFIG_CLIENT_WITHWATCHRECONNECTLIMIT | 16 | Maximum number of WebSocket watch reconnection attempts |

Additional Kubernetes client configuration options are available from the [Fabric8 Kubernetes Client documentation](https://github.com/fabric8io/kubernetes-client?tab=readme-ov-file#configuring-the-client).

## Web Server Configuration

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| server.port | SERVER_PORT | 8080 | Port number for the web server |
| server.tomcat.accesslog.pattern | SERVER_TOMCAT_ACCESSLOG_PATTERN | "request: method=%m uri=\"%U\" response: statuscode=%s bytes=%b duration=%D(ms) client: remoteip=%a user=%u useragent=\"%{User-Agent}i\"" | Pattern for Tomcat access logs |
| server.tomcat.accesslog.enabled | TOMCAT_ACCESSLOG_ENABLED | false | Enable or disable Tomcat access logging |

## Security Configuration

| Setting | Environment Variable | Default | Description                                                                                 |
|---------|---------------------|---------|---------------------------------------------------------------------------------------------|
| config.rest.security.mode | CONFIG_REST_SECURITY_MODE | oidc | Authentication mode (oidc, basic, or none)                                                  |
| config.rest.security.allowedRoles | SECURITY_ALLOWED_ROLES | ConfigAdmin,admin | Comma-separated list of roles with access permissions                                       |
| config.rest.security.principal-claim | SECURITY_USER_CLAIM | oid | JWT claim name for user identification                                                      |
| config.rest.security.roles-claim | SECURITY_ROLES_CLAIM | roles | JWT claim name for user roles                                                               |
| config.rest.security.jwk-key-uris | SECURITY_JWT_JWKS_URI | https://login.microsoftonline.com/common/discovery/v2.0/keys | URI for JSON Web Key Set                                                                    |
| config.rest.security.accepted-issuers | SECURITY_JWT_ACCEPTED_ISSUERS | - | List of accepted JWT token issuers                                                          |
| config.rest.security.accepted-issuers-aliases | SECURITY_JWT_ACCEPTED_ISSUERS_ALIAS | - | Aliases for accepted JWT token issuers                                                      |
| config.rest.security.accepted-audiences | DIAL_ADMIN_CLIENT_ID | - | Unique identifier assigned to DIAL Admin backend application by the authentication provider |
| config.rest.security.accepted-audiences | SECURITY_JWT_ACCEPTED_AUDIENCES | - | List of additional accepted JWT token audiences                                             |
| config.rest.security.disable-swagger-authorization | DISABLE_SWAGGER_AUTHORIZATION | false | Disable authorization for Swagger UI                                                        |

## Cloud Provider Configuration

### Azure Configuration
config.export.keyvault.type=azure

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| azure.auth.type | AUTH_AZURE_TYPE | none | Azure authentication method (values: credential,cli,managed) |
| azure.auth.clientId | AUTH_AZURE_CLIENT_ID | - | Azure service principal client ID |
| azure.auth.tenantId | AUTH_AZURE_TENANT_ID | - | Azure tenant ID |
| azure.auth.clientSecret | AUTH_AZURE_CLIENT_SECRET | - | Azure service principal client secret |
| azure.keyvault.vaultUrl | AZURE_KEY_VAULT_URL | - | URL of the Azure Key Vault |

### GCP Configuration
config.export.keyvault.type=gcp

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| gcp.keyvault.projectId | GCP_KEY_VAULT_PROJECT_ID | - | Google Cloud Platform project ID |

### AWS Configuration
config.export.keyvault.type=aws

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|

AWS Secrets Manager will be used for AWS services auth

### Hashivault (on premise server)
config.export.keyvault.type=vault

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| vault.uri | VAULT_URI          |         | URL of the hashivault |
| vault.token | VAULT_TOKEN     |         | hashivault access token |

## DIAL Core Configuration

| Setting | Environment Variable | Default        | Description |
|---------|---------------------|----------------|-------------|
| core.client.url | CORE_CLIENT_URL | localhost:8081 | URL of the DIAL Core service |
| core.prompts.metadata.default.limit | CORE_PROMPTS_METADATA_DEFAULT_LIMIT | 256 | Default limit on the number of items in the prompts metadata response from DIAL Core |

## OpenTelemetry Configuration

| Setting                             | Environment Variable        | Default            | Description                                       |
|-------------------------------------|-----------------------------|--------------------|---------------------------------------------------|
| otel.sdk.disabled                   | OTEL_SDK_DISABLED           | true               | Disable OpenTelemetry SDK                         |
| otel.service.name                   | OTEL_SERVICE_NAME           | dial-admin-backend | Service name                                      |
| otel.exporter.otlp.endpoint         | OTEL_EXPORTER_OTLP_ENDPOINT |                    | OpenTelemetry collector endpoint                  |
| otel.exporter.otlp.protocol         | OTEL_EXPORTER_OTLP_PROTOCOL |                    | Protocol for OpenTelemetry data export            |
| otel.logs.exporter                  | OTEL_LOGS_EXPORTER          | otlp               | Exporter for application logs                     |
| otel.traces.exporter                | OTEL_TRACES_EXPORTER        | otlp               | Exporter for distributed traces                   |
| otel.metrics.exporter               | OTEL_METRICS_EXPORTER       | otlp               | Exporter for application metrics                  |
| otel.resource.attributes            | OTEL_RESOURCE_ATTRIBUTES    |                    | Key-value pairs to be used as resource attributes |

## Actuator Configuration

| Setting | Environment Variable | Default | Description                  |
|---------|---------------------|---------|------------------------------|
| management.endpoints.web.exposure.include | MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE | prometheus,health | Actuator endpoints to expose |
| management.endpoint.health.show-details | MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS | always | Show health information      |
| management.server.port | MANAGEMENT_SERVER_PORT | 9464 | Actuator endpoints port      |

## Datasource Configuration

| Setting                        | Environment Variable              | Default                                             | Description                                                                                                                                            |
|--------------------------------|-----------------------------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| datasource.vendor              | DATASOURCE_VENDOR                 | H2                                                  | Datasource vendor: <ul><li>H2</li><li>POSTGRES</li><li>MS_SQL_SERVER</li></ul>                                                                         |
| datasource.auth.type           | DATASOURCE_AUTH_TYPE              | basic                                               | Datasource auth type: <ul><li>basic (username and password)</li><li>azure (see [Azure Configuration](#azure-configuration): azure.auth.type)</li></ul> |
| h2.datasource.url              | H2_DATASOURCE_URL                 | -                                                   | JDBC URL for H2 database connection                                                                                                                    |
|                                | H2_FILE                           | ./data/testdb                                       | H2 database file                                                                                                                                       |
|                                | H2_OPS                            | CIPHER=AES;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE | H2 database connection options                                                                                                                         |
| h2.datasource.masterKey        | H2_DATASOURCE_MASTERKEY           | -                                                   | Master key for H2 database encryption                                                                                                                  |
| h2.datasource.encryptedFileKey | H2_DATASOURCE_ENCRYPTEDFILEKEY    | -                                                   | Encrypted file key for H2 database                                                                                                                     |
| h2.datasource.password         | H2_DATASOURCE_PASSWORD            | -                                                   | Password for H2 database access                                                                                                                        |
| postgres.datasource.url        | POSTGRES_DATASOURCE_URL           | -                                                   | JDBC URL for Postgres database connection                                                                                                              |
|                                | POSTGRES_HOST                     | localhost                                           | Postgres database host                                                                                                                                 |
|                                | POSTGRES_PORT                     | 5432                                                | Postgres database port                                                                                                                                 |
|                                | POSTGRES_DATABASE                 | testdb                                              | Postgres database name                                                                                                                                 |
|                                | POSTGRES_OPS                      | -                                                   | Postgres database connection options                                                                                                                   |
| postgres.datasource.username   | POSTGRES_DATASOURCE_USERNAME      | postgres                                            | Username for Postgres database access                                                                                                                  |
| postgres.datasource.password   | POSTGRES_DATASOURCE_PASSWORD      | postgres                                            | Password for Postgres database access                                                                                                                  |
| sqlserver.datasource.url       | SQLSERVER_DATASOURCE_URL          | -                                                   | JDBC URL for MSSQL Server database connection                                                                                                          |
|                                | MS_SQL_SERVER_HOST                | localhost                                           | MSSQL Server database host                                                                                                                             |
|                                | MS_SQL_SERVER_PORT                | 1433                                                | MSSQL Server database port                                                                                                                             |
|                                | MS_SQL_SERVER_DATABASE            | testdb                                              | MSSQL Server database name                                                                                                                             |
|                                | MS_SQL_SERVER_OPS                 | encrypt=true;                                       | MSSQL Server database connection options                                                                                                               |
| sqlserver.datasource.username  | MS_SQL_SERVER_DATASOURCE_USERNAME | sa                                                  | Username for MSSQL Server database access                                                                                                              |
| sqlserver.datasource.password  | MS_SQL_SERVER_DATASOURCE_PASSWORD | SQLServerPassword1                                  | Password for MSSQL Server database access                                                                                                              |
| spring.jpa.hibernate.ddl-auto  | SPRING_JPA_HIBERNATE_DDL_AUTO     | validate                                            | Hibernate schema generation strategy                                                                                                                   |
| spring.flyway.enabled          | SPRING_FLYWAY_ENABLED             | true                                                | Enable or disable Flyway database migrations                                                                                                           |

When using MS_SQL_SERVER we recommend to set case-sensitive, accept-sensitive database collation, e.g. SQL_Latin1_General_CP1_CS_AS. See [Collation and Unicode support](https://learn.microsoft.com/en-us/sql/relational-databases/collations/collation-and-unicode-support).

## Metrics Configuration

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| metrics.enabled | METRICS_ENABLED | false | Enable or disable metrics collection |
| metrics.configFile.contentEnvVar | METRICS_CONFIGFILE_CONTENTENVVAR | METRICS_CONFIG_CONTENT | Environment variable containing metrics configuration |
| metrics.configFile.location | METRICS_CONFIGFILE_LOCATION | data/admin/metric.config.json | Path to metrics configuration file |
| metrics.datasource.influx.defaultPageSize | METRICS_DATASOURCE_INFLUX_DEFAULTPAGESIZE | 100 | Default page size for InfluxDB queries |

## Logging Configuration

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| logging.level.org.springframework.security | LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY | DEBUG | Log level for Spring Security |
| logging.level.com.epam.aidial.cfg | APP_LOG_LEVEL | INFO | Log level for application code |
| logging.level.org.hibernate.SQL | LOGGING_LEVEL_ORG_HIBERNATE_SQL | INFO | Log level for Hibernate SQL statements |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.enabled | CUSTOMIZABLE_TRACE_INTERCEPTOR_ENABLED | true | Enable or disable method tracing |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.ENTER | CUSTOMIZABLE_TRACE_INTERCEPTOR_ENTER_MESSAGE | 'Enter: $[methodName](): $[arguments]' | Format for method entry logs |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.EXIT | CUSTOMIZABLE_INTERCEPTOR_EXIT_MESSAGE | 'Exit: $[methodName]() : in $[invocationTime] ms, returnValue: $[returnValue]' | Format for method exit logs |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.EXCEPTION | CUSTOMIZABLE_TRACE_INTERCEPTOR_EXCEPTION_MESSAGE | 'Exception: $[methodName]() : in $[invocationTime] ms' | Format for exception logs |
| logger.configuration.path | LOGGER_CONFIGURATION_PATH | log-config/logging.levels.json | Path to logger configuration file |
| logger.configuration.interval | LOGGER_CONFIGURATION_INTERVAL | 10 | Interval in seconds for checking logger configuration updates |

## Retry Configuration

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| feign.retry.period | FEIGN_RETRY_PERIOD | 5000 | Initial retry delay in milliseconds |
| feign.retry.maxPeriod | FEIGN_RETRY_MAXPERIOD | 10000 | Maximum retry delay in milliseconds |
| feign.retry.maxAttempts | FEIGN_RETRY_MAXATTEMPTS | 3 | Maximum number of retry attempts |
| feign.retry.errorCodes | FEIGN_RETRY_ERRORCODES | 408,429,500,502,503,504 | HTTP status codes that trigger retries |
| prompts.import.consecutiveErrorsThreshold | PROMPTS_IMPORT_CONSECUTIVE_ERRORS_THRESHOLD | 2 | Maximum number of consecutive errors allowed during prompts import |
| files.import.consecutiveErrorsThreshold   | FILES_IMPORT_CONSECUTIVE_ERRORS_THRESHOLD | 2 | Maximum number of consecutive errors allowed during files import   |

## Export/Import Configuration

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| config.import.configsMaxCount | IMPORT_CONFIGS_MAX_COUNT | 64 | Maximum number of files allowed for a single import config operation |

## Additional Entities Configuration
*(Temporary configuration - will be implemented as managed entities inside admin app)*

| Setting | Environment Variable | Default | Description |
|---------|---------------------|---------|-------------|
| config.env.tokenizers.json | CONFIG_ENV_TOKENIZERS_JSON | - | Preconfigured DIAL tokenizers list in JSON format |
