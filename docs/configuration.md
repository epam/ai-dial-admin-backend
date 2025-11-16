# Configuration Guide

This document provides a comprehensive list of all configurable properties in the AIDIAL Admin Panel Backend.

## Table of Contents

- [AIDIAL Config File Export Configuration](#aidial-config-file-export-configuration)
- [AIDIAL Config File Import Configuration](#aidial-config-file-import-configuration)
- [Kubernetes Configuration](#kubernetes-configuration)
- [Web Server Configuration](#web-server-configuration)
- [Servlet Configuration](#servlet-configuration)
- [Security Configuration](#security-configuration)
- [Cloud Provider Configuration](#cloud-provider-configuration)
- [DIAL Core Configuration](#dial-core-configuration)
- [OpenTelemetry Configuration](#opentelemetry-configuration)
- [Actuator Configuration](#actuator-configuration)
- [Datasource Configuration](#datasource-configuration)
- [Metrics Configuration](#metrics-configuration)
- [Logging Configuration](#logging-configuration)
- [Retry Configuration](#retry-configuration)
- [Validation Configuration](#validation-configuration)

## AIDIAL Config File Export Configuration

| Setting                                  | Environment Variable                     | Default              | Required | Applied when | Description                                                                                                                                 |
|------------------------------------------|------------------------------------------|----------------------|----------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------|
| config.export.enabled                    | CONFIG_EXPORT_ENABLED                    | true                 | No       | -         | Enables or disables DIAL Core configuration file scheduled export functionality                                                                       |
| config.version.target                    | CORE_CONFIG_VERSION                      | latest               | No  (recommended to adjust for target environment)       | -         | Version of DIAL Core configuration used in file export. functionality                                                                        |
| config.version.autoDetect.enabled        | ENABLE_CORE_CONFIG_VERSION_AUTO_DETECT   | false                | Enable auto-detection of DIAL Core version                                                                                                  |
| config.version.autoDetect.cacheExpirationMs    | CORE_VERSION_CACHE_EXPIRATION_MS    | 300000              | Cached version expiration in milliseconds for Core version auto-detection                                                                   |
| config.export.syncPeriod                 | CONFIG_EXPORT_SYNCPERIOD                 | 15000                | No       | -         | Interval in milliseconds for DIAL configuration export                                                                                      |
| config.reload.enabled                    | ENABLE_CONFIG_RELOAD                     | true                 | No       | -         | Enable writing config to destination storage and calling the /reload_config endpoint on core |
| config.reload.delay                      | DELAY_CONFIG_RELOAD_MILLISECONDS         | 5000                 | No       | config.reload.enabled=true         | Delay in milliseconds before calling the /reload_config endpoint on core after writing to destination storage (e.g., configMap sync period) |
| config.export.storageType                | CONFIG_EXPORT_STORAGETYPE                | LOCAL_FILE           | No (recommended to adjust for target environment)      | -         | Type of storage for DIAL configuration export (KUBE_SECRET, CONFIG_MAP, LOCAL_FILE)                                                         |
| config.export.outputFile.path            | CONFIG_EXPORT_OUTPUTFILE_PATH            | data/export/out.json | No       | config.export.storageType=LOCAL_FILE         | Path for configuration file when using LOCAL_FILE storage type |
| config.export.configMap.names            | CONFIG_EXPORT_CONFIGMAP_NAMES            | -     | Yes      | config.export.storageType=CONFIG_MAP         | Comma separated names of the ConfigMaps used for DIAL configuration export                                                                  |
| config.export.configMap.key              | CONFIG_EXPORT_CONFIGMAP_KEY              | env.config.json      | No (recommended to adjust for target environment)      | config.export.storageType=CONFIG_MAP         | Key in ConfigMap used for DIAL configuration export                                                                                         |                                                                          
| config.export.kubeSecret.names           | CONFIG_EXPORT_KUBESECRET_NAMES           | -       | Yes       | config.export.storageType=KUBE_SECRET     | Comma separated names of the Kubernetes Secrets used for DIAL configuration export                                                          |
| config.export.kubeSecret.key             | CONFIG_EXPORT_KUBESECRET_KEY             | kube-secret-key      | No (recommended to adjust for target environment)      | config.export.storageType=KUBE_SECRET         | Key in Kubernetes Secret used for DIAL configuration export                                                                                 |
| config.export.keyvault.type              | CONFIG_EXPORT_KEYVAULT_TYPE              | none                 | No (recommended to adjust for target environment)      | -         | Type of keyvault storage for secret values (none, azure, vault, aws, gcp)                                                                   |
| config.export.keyvault.secretNames       | CONFIG_EXPORT_KEYVAULT_SECRETNAMES       | -                    | Yes  | config.export.keyvault.type in [azure, aws, gcp]         | Names of secrets in keyvault (used when keyvault.type is azure, vault, or aws)                                                              |
| config.export.keyvault.secretPath        | CONFIG_EXPORT_KEYVAULT_SECRETPATH        | -                    | Yes | config.export.keyvault.type == vault         | Path to secrets in keyvault (used when keyvault.type is vault)                                                                              |
| config.export.keyvault.expiration.period | CONFIG_EXPORT_KEYVAULT_EXPIRATION_PERIOD | 3                    | No       | -         | Expiration period for keyvault values                                                                                                       |
| config.export.keyvault.expiration.unit   | CONFIG_EXPORT_KEYVAULT_EXPIRATION_UNIT   | MONTHS               | No       | -         | Unit of time for keyvault value expiration                                                                                                  |
| config.export.createResources            | CONFIG_EXPORT_CREATE_RESOURCES           | false                | No       | -         | If true, create resources where config is exported if they don't already exist                                                              |

## AIDIAL Config File Import Configuration

| Setting                                     | Environment Variable                   | Default | Required | Applied when | Description                                                                                                                       |
|---------------------------------------------|----------------------------------------|---------|----------|--------------|-----------------------------------------------------------------------------------------------------------------------------------|
| config.import.configsMaxCount               | IMPORT_CONFIGS_MAX_COUNT               | 64      | No       | -            | Maximum number of files allowed for a single import config operation                                                              |
| config.import.autoImportOnBootstrap.enabled | ENABLE_CONFIG_AUTO_IMPORT_ON_BOOTSTRAP | false   | No       | -            | Enable core config auto import from the same location where export is configured. Auto import runs once on startup if DB is empty |

## Kubernetes Configuration

Applied when: config.export.storageType=CONFIG_MAP|KUBE_SECRET

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| kubernetes-config.connectType | KUBERNETES_CONFIG_CONNECTTYPE | CONFIG_FILE | No | - | Kubernetes connection type (CONFIG_FILE or TOKEN) |
| kubernetes-config.masterUrl | KUBERNETES_CONFIG_MASTERURL | url | Yes | kubernetes-config.connectType=TOKEN | Kubernetes master URL |
| kubernetes-config.oauthToken | KUBERNETES_CONFIG_OAUTHTOKEN | token | Yes | kubernetes-config.connectType=TOKEN | Kubernetes OAuth token |
| kubernetes-config.trustCerts | KUBERNETES_CONFIG_TRUSTCERTS | false | No | - | Trust all Kubernetes certificates |
| kubernetes-config.namespace | KUBERNETES_CONFIG_NAMESPACE | default | No (recommended to adjust for target environment) | - | Kubernetes namespace for operations |
| kubernetes-config.client.maxConcurrentRequests | KUBERNETES_CONFIG_CLIENT_MAXCONCURRENTREQUESTS | 64 | No | - | Maximum number of concurrent requests to Kubernetes API |
| kubernetes-config.client.maxConcurrentRequestsPerHost | KUBERNETES_CONFIG_CLIENT_MAXCONCURRENTREQUESTSPERHOST | 64 | No | - | Maximum number of concurrent requests per Kubernetes host |
| kubernetes-config.client.requestRetryBackoffLimit | KUBERNETES_CONFIG_CLIENT_REQUESTRETRYBACKOFFLIMIT | 10 | No | - | Maximum number of retry attempts for failed requests |
| kubernetes-config.client.requestTimeout | KUBERNETES_CONFIG_CLIENT_REQUESTTIMEOUT | 20000 | No | - | Request timeout in milliseconds |
| kubernetes-config.client.withWebsocketPingInterval | KUBERNETES_CONFIG_CLIENT_WITHWEBSOCKETPINGINTERVAL | 120000 | No | - | WebSocket ping interval in milliseconds |
| kubernetes-config.client.withWatchReconnectLimit | KUBERNETES_CONFIG_CLIENT_WITHWATCHRECONNECTLIMIT | 16 | No | - | Maximum number of WebSocket watch reconnection attempts |
| kubernetes-config.client.operationTimeoutMs | KUBERNETES_CONFIG_CLIENT_OPERATIONTIMEOUTMS | 300000 | No | - | Kubernetes operation timeout (update config map, read secret, etc.) |

Additional Kubernetes client configuration options are available from the [Fabric8 Kubernetes Client documentation](https://github.com/fabric8io/kubernetes-client?tab=readme-ov-file#configuring-the-client).

## Web Server Configuration

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| server.port | SERVER_PORT | 8080 | No | - | Port number for the web server |
| server.tomcat.accesslog.pattern | SERVER_TOMCAT_ACCESSLOG_PATTERN | "request: method=%m uri=\"%U\" response: statuscode=%s bytes=%b duration=%D(ms) client: remoteip=%a user=%u useragent=\"%{User-Agent}i\"" | No | server.tomcat.accesslog.enabled=true | Pattern for Tomcat access logs |
| server.tomcat.accesslog.enabled | TOMCAT_ACCESSLOG_ENABLED | false | No | - | Enable or disable Tomcat access logging |
| server.tomcat.maxPartCount | SERVER_TOMCAT_MAX_PART_COUNT | 64 | false | - |Maximum total number of parts permitted in a multipart/form-data request. Requests that exceed this limit will be rejected. A value of less than 0 means no limit. |

## Servlet Configuration

| Setting                                 | Environment Variable               | Default | Required | Applied when | Description      |
|-----------------------------------------|------------------------------------|---------|----------|--------------|------------------|
| spring.servlet.multipart.maxFileSize    | SERVLET_MULTIPART_MAX_FILE_SIZE    | 4MB     | No       | -            | Max file size    |
| spring.servlet.multipart.maxRequestSize | SERVLET_MULTIPART_MAX_REQUEST_SIZE | 64MB    | No       | -            | Max request size |

## Security Configuration

### General Settings

| Setting                                            | Environment Variable          | Default           | Required                                          | Applied when                   | Description                                           |
|----------------------------------------------------|-------------------------------|-------------------|---------------------------------------------------|--------------------------------|-------------------------------------------------------|
| config.rest.security.mode                          | CONFIG_REST_SECURITY_MODE     | none              | No (recommended to adjust for target environment) | -                              | Authentication mode (oidc, basic, or none)            |
| config.rest.security.allowedRoles                  | -                             | ConfigAdmin,admin | No (recommended to adjust for target environment) | config.rest.security.mode=oidc | Comma-separated list of roles with access permissions |
| config.rest.security.principal-claim               | SECURITY_USER_CLAIM           | oid               | No (recommended to adjust for target environment) | config.rest.security.mode=oidc | JWT claim name for user identification                |
| config.rest.security.disable-swagger-authorization | DISABLE_SWAGGER_AUTHORIZATION | false             | No                                                | config.rest.security.mode=oidc | Disable authorization for Swagger UI                  |

### Identity Providers Configuration

Applied when: config.rest.security.mode=oidc
The configuration is defined in environment variables

| Setting                   | Environment Variable (as example) | Required | Applied when                   | Description                                                                                 |
|---------------------------|-----------------------------------|----------|--------------------------------|---------------------------------------------------------------------------------------------|
| providers.*.issuer        | providers.azure.issuer            | Yes      | config.rest.security.mode=oidc | List of accepted JWT token issuers for the provider                                         |
| providers.*.jwk-set-uri   | providers.azure.jwk-set-uri       | Yes      | config.rest.security.mode=oidc | URI for JSON Web Key Set for the provider                                                   |
| providers.*.aliases       | providers.azure.aliases           | No       | config.rest.security.mode=oidc | Aliases for accepted JWT token issuers for the provider(only for Azure provider)            |
| providers.*.audiences     | providers.azure.audiences         | Yes      | config.rest.security.mode=oidc | Unique identifier assigned to DIAL Admin backend application by the authentication provider |
| providers.*.role-claims   | providers.azure.role-claims       | No       | config.rest.security.mode=oidc | JWT claim name for user roles for the provider                                              |
| providers.*.allowed-roles | providers.azure.allowed-roles     | No       | config.rest.security.mode=oidc | Comma-separated list of roles with access permissions for the provider                      |

## Cloud Provider Configuration

### Azure Configuration

Applied when: config.export.keyvault.type=azure

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| azure.auth.type | AUTH_AZURE_TYPE | none | Yes | - | Azure authentication method (values: credential,cli,managed) |
| azure.auth.clientId | AUTH_AZURE_CLIENT_ID | - | Yes | azure.auth.type=credential | Azure service principal client ID |
| azure.auth.tenantId | AUTH_AZURE_TENANT_ID | - | Yes | azure.auth.type in [cli,credential] | Azure tenant ID |
| azure.auth.clientSecret | AUTH_AZURE_CLIENT_SECRET | - | Yes | azure.auth.type=credential | Azure service principal client secret |
| azure.keyvault.vaultUrl | AZURE_KEY_VAULT_URL | - | Yes | - | URL of the Azure Key Vault |

### GCP Configuration

Applied when: config.export.keyvault.type=gcp

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| gcp.keyvault.projectId | GCP_KEY_VAULT_PROJECT_ID | - | Yes | - | Google Cloud Platform project ID |

### AWS Configuration

Applied when: config.export.keyvault.type=aws

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|

AWS Secrets Manager will be used for AWS services auth

### Hashivault (on premise server)

Applied when: config.export.keyvault.type=vault

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| vault.uri | VAULT_URI          |         | Yes | - | URL of the hashivault |
| vault.token | VAULT_TOKEN     |         | Yes | - | hashivault access token |

## DIAL Core Configuration

| Setting                                  | Environment Variable                     | Default | Required | Applied when | Description                                                                              |
|------------------------------------------|------------------------------------------|---------|----------|-----------|------------------------------------------------------------------------------------------|
| core.client.url                          | CORE_CLIENT_URL                          | localhost:8081 | No (recommended to adjust for target environment) | - | URL of the DIAL Core service                                                             |
| core.prompts.metadata.default.limit      | CORE_PROMPTS_METADATA_DEFAULT_LIMIT      | 256 | No | - | Default limit on the number of items in the prompts metadata response from DIAL Core     |
| core.applications.metadata.default.limit | CORE_APPLICATIONS_METADATA_DEFAULT_LIMIT | 256 | No | - | Default limit on the number of items in the applications metadata response from DIAL Core |
| core.toolsets.metadata.default.limit     | CORE_TOOLSETS_METADATA_DEFAULT_LIMIT     | 256 | No | - | Default limit on the number of items in the toolsets metadata response from DIAL Core    |

## OpenTelemetry Configuration

| Setting                             | Environment Variable        | Default            | Required | Applied when | Description                                       |
|-------------------------------------|-----------------------------|--------------------|----------|-----------|---------------------------------------------------|
| otel.sdk.disabled                   | OTEL_SDK_DISABLED           | true               | No       | -         | Disable OpenTelemetry SDK                         |
| otel.service.name                   | OTEL_SERVICE_NAME           | dial-admin-backend | No       | -         | Service name                                      |
| otel.exporter.otlp.endpoint         | OTEL_EXPORTER_OTLP_ENDPOINT |                    | Yes      | otel.sdk.disabled=false         | OpenTelemetry collector endpoint                  |
| otel.exporter.otlp.protocol         | OTEL_EXPORTER_OTLP_PROTOCOL |                    | Yes      | otel.sdk.disabled=false         | Protocol for OpenTelemetry data export            |
| otel.logs.exporter                  | OTEL_LOGS_EXPORTER          | otlp               | No       | -         | Exporter for application logs                     |
| otel.traces.exporter                | OTEL_TRACES_EXPORTER        | otlp               | No       | -         | Exporter for distributed traces                   |
| otel.metrics.exporter               | OTEL_METRICS_EXPORTER       | otlp               | No       | -         | Exporter for application metrics                  |
| otel.resource.attributes            | OTEL_RESOURCE_ATTRIBUTES    |                    | No       | -         | Key-value pairs to be used as resource attributes |

## Actuator Configuration

| Setting | Environment Variable | Default | Required | Applied when | Description                  |
|---------|---------------------|---------|----------|-----------|------------------------------|
| management.endpoints.web.exposure.include | MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE | prometheus,health | No | - | Actuator endpoints to expose |
| management.endpoint.health.show-details | MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS | always | No | - | Show health information      |
| management.server.port | MANAGEMENT_SERVER_PORT | 9464 | No | - | Actuator endpoints port      |

## Datasource Configuration

| Setting                        | Environment Variable              | Default                                             | Required | Applied when | Description                                                                                                                                            |
|--------------------------------|-----------------------------------|-----------------------------------------------------|----------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| datasource.vendor              | DATASOURCE_VENDOR                 | H2   | No (recommended to adjust for target environment)       | -         | Datasource vendor: <ul><li>H2</li><li>POSTGRES</li><li>MS_SQL_SERVER</li></ul>                                                                         |
| datasource.auth.type           | DATASOURCE_AUTH_TYPE              | basic                                               | No       | -         | Datasource auth type: <ul><li>basic (username and password)</li><li>azure (see [Azure Configuration](#azure-configuration): azure.auth.type)</li></ul> |
| h2.datasource.url              | H2_DATASOURCE_URL                 | jdbc:h2:file:${H2_FILE};${H2_OPS}                   | No       | datasource.vendor=H2 | JDBC URL for H2 database connection                                                                                                                    |
|                                | H2_FILE                           | ./data/testdb | No (recommended to adjust for target environment)      | datasource.vendor=H2 | H2 database file                                                                                                                                       |
|                                | H2_OPS                            | CIPHER=AES;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE | No       | datasource.vendor=H2 | H2 database connection options                                                                                                                         |
| h2.datasource.masterKey        | H2_DATASOURCE_MASTERKEY           | -                                                   | Yes       | datasource.vendor=H2 | Master key for H2 database encryption                                                                                                                  |
| h2.datasource.encryptedFileKey | H2_DATASOURCE_ENCRYPTEDFILEKEY    | -                                                   | Yes       | datasource.vendor=H2 | Encrypted file key for H2 database                                                                                                                     |
| h2.datasource.password         | H2_DATASOURCE_PASSWORD            | -                                                   | Yes       | datasource.vendor=H2 | Password for H2 database access                                                                                                                        |
| postgres.datasource.url        | POSTGRES_DATASOURCE_URL           | jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DATABASE}?${POSTGRES_OPS:} | No       | datasource.vendor=POSTGRES | JDBC URL for Postgres database connection                                                                                                              |
|                                | POSTGRES_HOST                     | localhost| No (recommended to adjust for target environment)        | datasource.vendor=POSTGRES | Postgres database host                                                                                                                                 |
|                                | POSTGRES_PORT                     | 5432                                                | No       | datasource.vendor=POSTGRES | Postgres database port                                                                                                                                 |
|                                | POSTGRES_DATABASE                 | testdb | No (recommended to adjust for target environment)       | datasource.vendor=POSTGRES | Postgres database name                                                                                                                                 |
|                                | POSTGRES_OPS                      | -                                                   | No       | datasource.vendor=POSTGRES | Postgres database connection options                                                                                                                   |
| postgres.datasource.username   | POSTGRES_DATASOURCE_USERNAME      | postgres                                            | No (recommended to adjust for target environment)      | datasource.vendor=POSTGRES and datasource.auth.type=basic | Username for Postgres database access                                                                                                                  |
| postgres.datasource.password   | POSTGRES_DATASOURCE_PASSWORD      | postgres                                            | No (recommended to adjust for target environment)      | datasource.vendor=POSTGRES and datasource.auth.type=basic | Password for Postgres database access                                                                                                                  |
| sqlserver.datasource.url       | SQLSERVER_DATASOURCE_URL          | jdbc:sqlserver://${MS_SQL_SERVER_HOST}:${MS_SQL_SERVER_PORT};database=${MS_SQL_SERVER_DATABASE};${MS_SQL_SERVER_OPS}  | No       | datasource.vendor=MS_SQL_SERVER | JDBC URL for MSSQL Server database connection                                                                                                          |
|                                | MS_SQL_SERVER_HOST                | localhost  | No (recommended to adjust for target environment)      | datasource.vendor=MS_SQL_SERVER | MSSQL Server database host                                                                                                                             |
|                                | MS_SQL_SERVER_PORT                | 1433                                                | No       | datasource.vendor=MS_SQL_SERVER | MSSQL Server database port                                                                                                                             |
|                                | MS_SQL_SERVER_DATABASE            | testdb    | No (recommended to adjust for target environment)      | datasource.vendor=MS_SQL_SERVER | MSSQL Server database name                                                                                                                             |
|                                | MS_SQL_SERVER_OPS                 | encrypt=false;   | No (recommended to adjust for target environment)      | datasource.vendor=MS_SQL_SERVER | MSSQL Server database connection options                                                                                                               |
| sqlserver.datasource.username  | MS_SQL_SERVER_DATASOURCE_USERNAME | sa                                                  | No (recommended to adjust for target environment)      | datasource.vendor=MS_SQL_SERVER and datasource.auth.type=basic | Username for MSSQL Server database access                                                                                                              |
| sqlserver.datasource.password  | MS_SQL_SERVER_DATASOURCE_PASSWORD | SQLServerPassword1                                  | No (recommended to adjust for target environment)      | datasource.vendor=MS_SQL_SERVER and datasource.auth.type=basic | Password for MSSQL Server database access                                                                                                              |
| spring.jpa.hibernate.ddl-auto  | SPRING_JPA_HIBERNATE_DDL_AUTO     | validate                                            | No       | -         | Hibernate schema generation strategy                                                                                                                   |
| spring.flyway.enabled          | SPRING_FLYWAY_ENABLED             | true                                                | No       | -         | Enable or disable Flyway database migrations                                                                                                           |

When using MS_SQL_SERVER we recommend to set case-sensitive, accept-sensitive database collation, e.g. SQL_Latin1_General_CP1_CS_AS. See [Collation and Unicode support](https://learn.microsoft.com/en-us/sql/relational-databases/collations/collation-and-unicode-support).

ai-dial-admin-backend/secrets-utils/generate_h2_secrets.sh can help to generate H2_DATASOURCE_MASTERKEY/H2_DATASOURCE_ENCRYPTEDFILEKEY/H2_DATASOURCE_PASSWORD if H2 db is used.

## Metrics Configuration

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| metrics.enabled | METRICS_ENABLED | false | No | - | Enable or disable metrics collection |
| metrics.configFile.contentEnvVar | METRICS_CONFIGFILE_CONTENTENVVAR | METRICS_CONFIG_CONTENT | No | metrics.enabled=true | Environment variable containing metrics configuration |
| metrics.configFile.location | METRICS_CONFIGFILE_LOCATION | data/admin/metric.config.json | No | metrics.enabled and env[metrics.configFile.contentEnvVar] does not defined | Path to metrics configuration file |
| metrics.datasource.influx.defaultPageSize | METRICS_DATASOURCE_INFLUX_DEFAULTPAGESIZE | 100 | No | - | Default page size for InfluxDB queries |
|  | METRICS_STORAGE_HOST | - | Yes | metrics.enabled=true and default metrics config used | URL for InfluxDB database connection |
|  | METRICS_STORAGE_ORG | dial | No | metrics.enabled=true and default metrics config used | Inlux organization with metrics |
|  | METRICS_STORAGE_TOKEN | - | Yes | metrics.enabled=true and default metrics config used | Token for InfluxDB database connection  |

metrics/telemetry functionality in admin panel reads data produced by https://github.com/epam/ai-dial-analytics-realtime.

example of json file provided via METRICS_CONFIGFILE_CONTENTENVVAR or METRICS_CONFIGFILE_LOCATION can be found at "ai-dial-admin-backend/src/main/resources/metric.config.json"

## Logging Configuration

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| logging.level.org.springframework.security | LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY | INFO | No | - | Log level for Spring Security |
| logging.level.com.epam.aidial.cfg | APP_LOG_LEVEL | INFO | No | - | Default Log level for application code |
| logging.level.org.hibernate.SQL | LOGGING_LEVEL_ORG_HIBERNATE_SQL | INFO | No | - | Default Log level for Hibernate SQL statements |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.enabled | CUSTOMIZABLE_TRACE_INTERCEPTOR_ENABLED | true | No | - | Enable or disable method tracing |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.ENTER | CUSTOMIZABLE_TRACE_INTERCEPTOR_ENTER_MESSAGE | 'Enter: $[methodName](): $[arguments]' | No | - | Format for method entry logs |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.EXIT | CUSTOMIZABLE_INTERCEPTOR_EXIT_MESSAGE | 'Exit: $[methodName]() : in $[invocationTime] ms, returnValue: $[returnValue]' | No | - | Format for method exit logs |
| com.epam.aidial.cfg.configuration.customizable-trace-interceptor.messages.EXCEPTION | CUSTOMIZABLE_TRACE_INTERCEPTOR_EXCEPTION_MESSAGE | 'Exception: $[methodName]() : in $[invocationTime] ms' | No | - | Format for exception logs |
| logger.configuration.path | LOGGER_CONFIGURATION_PATH | log-config/logging.levels.json | No | - | Path to logger configuration file |
| logger.configuration.interval | LOGGER_CONFIGURATION_INTERVAL | 10 | No | - | Interval in seconds for checking logger configuration updates |



## Retry Configuration

| Setting                                   | Environment Variable                        | Default                 | Required | Applied when | Description                                                        |
|-------------------------------------------|---------------------------------------------|-------------------------|-----------------------------------------------------------------|-----------|-------------|
| feign.retry.period                        | FEIGN_RETRY_PERIOD                          | 10000                   | No | - | Initial retry delay in milliseconds                                |
| feign.retry.maxPeriod                     | FEIGN_RETRY_MAXPERIOD                       | 15000                   | No | - | Maximum retry delay in milliseconds                                |
| feign.retry.maxAttempts                   | FEIGN_RETRY_MAXATTEMPTS                     | 5                       | No | - | Maximum number of retry attempts                                   |
| feign.retry.errorCodes                    | FEIGN_RETRY_ERRORCODES                      | 408,429,500,502,503,504 | No | - | HTTP status codes that trigger retries                             |
| prompts.import.consecutiveErrorsThreshold | PROMPTS_IMPORT_CONSECUTIVE_ERRORS_THRESHOLD | 2                       | No | - | Maximum number of consecutive errors allowed during prompts import |
| files.import.consecutiveErrorsThreshold   | FILES_IMPORT_CONSECUTIVE_ERRORS_THRESHOLD   | 2                       | No | - | Maximum number of consecutive errors allowed during files import   |

## Additional Entities Configuration
*(Temporary configuration - will be implemented as managed entities inside admin app)*

| Setting | Environment Variable | Default | Required | Applied when | Description |
|---------|---------------------|---------|----------|-----------|-------------|
| config.env.tokenizers.json | CONFIG_ENV_TOKENIZERS_JSON | - | No | - | Preconfigured DIAL tokenizers list in JSON format |

## Plugins Configuration

| Setting                                              | Environment Variable                                    | Default         | Required | Applied when                                               | Description                                              |
|------------------------------------------------------|---------------------------------------------------------|-----------------|----------|------------------------------------------------------------|----------------------------------------------------------|
| plugins.deployment.manager.client.url                | PLUGINS_DEPLOYMENT_MANAGER_CLIENT_URL                   | url-placeholder | No       | -                                                          | Deployment manager client URL                            |
| plugins.deployment.manager.cache.expiration.interval | PLUGINS_DEPLOYMENT_MANAGER_CACHE_EXPIRATION_INTERVAL_MS | 300000          | No       | -                                                          | Expiration interval (in ms) of deployment manager cache  |
| plugins.deployment.manager.endpoint.refresh.enabled  | ENABLE_PLUGINS_DEPLOYMENT_MANAGER_ENDPOINT_REFRESH      | false           | No       | -                                                          | Enable deployment manager endpoint refresh               |
| plugins.deployment.manager.endpoint.refresh.interval | PLUGINS_DEPLOYMENT_MANAGER_ENDPOINT_REFRESH_INTERVAL_MS | 360000          | No       | plugins.deployment.manager.endpoint.refresh.enabled = true | Refresh interval (in ms) of deployment manager endpoints |

## Validation Configuration

Allows specifying additional environment-specific entity name validation patterns.

| Setting                             | Environment Variable                          | Default | Required | Applied when | Description                                     |
|-------------------------------------|-----------------------------------------------|---------|----------|-----------|-------------------------------------------------|
| validation.role.name                | ROLE_NAME_VALIDATION_PATTERN                  | - | No | - | Validation pattern for Role name                |
| validation.adapter.name             | ADAPTER_NAME_VALIDATION_PATTERN               | - | No | - | Validation pattern for Adapter name             |
| validation.addon.name               | ADDON_NAME_VALIDATION_PATTERN                 | - | No | - | Validation pattern for Addon name               |
| validation.application.name         | APPLICATION_NAME_VALIDATION_PATTERN           | - | No | - | Validation pattern for Application name         |
| validation.assistant.name           | ASSISTANT_NAME_VALIDATION_PATTERN             | - | No | - | Validation pattern for Assistant name           |
| validation.interceptor.name         | INTERCEPTOR_NAME_VALIDATION_PATTERN           | - | No | - | Validation pattern for Interceptor name         |
| validation.interceptorRunner.name   | INTERCEPTOR_RUNNER_NAME_VALIDATION_PATTERN    | - | No | - | Validation pattern for InterceptorRunner name   |
| validation.key.name                 | KEY_NAME_VALIDATION_PATTERN                   | - | No | - | Validation pattern for Key name                 |
| validation.model.name               | MODEL_NAME_VALIDATION_PATTERN                 | - | No | - | Validation pattern for Model name               |
| validation.route.name               | ROUTE_NAME_VALIDATION_PATTERN                 | - | No | - | Validation pattern for Route name               |
| validation.applicationTypeSchema.id | APPLICATION_TYPE_SCHEMA_ID_VALIDATION_PATTERN | - | No | - | Validation pattern for ApplicationTypeSchema id |
| validation.toolSet.name             | TOOLSET_NAME_VALIDATION_PATTERN               | - | No | - | Validation pattern for ToolSet name             |