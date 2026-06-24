# Infrastructure Changelog

All notable infrastructure-relevant changes to this project will be documented in this file. 

## 0.17.0

### Added

#### Configuration Management
- `CORE_CONVERSATIONS_METADATA_DEFAULT_LIMIT` — Default limit on the number of items in the conversations metadata response from DIAL Core (default: `256`)
- `CONVERSATIONS_IMPORT_CONSECUTIVE_ERRORS_THRESHOLD` — Maximum number of consecutive errors allowed during conversations import (default: `2`)

---

### Changed

#### Security & RBAC
- `config.rest.security.default.roles-mapping` default changed from `{}` to `{"ConfigAdmin":["FULL_ADMIN"],"admin":["FULL_ADMIN"]}`

---

### Removed

#### Security & RBAC
- `config.rest.security.default.allowedRoles` — no longer supported
- `providers.*.allowed-roles` — no longer supported

#### Observability
- `METRICS_INFLUX2_DEFAULT_PAGE_SIZE` — no longer supported
- `METRICS_INFLUX3_DEFAULT_PAGE_SIZE` — no longer supported

---

## 0.16.0

### Added

#### Observability

- `METRICS_INFLUX_CONNECT_TIMEOUT` — InfluxDB HTTP client connection timeout in seconds (default: `10`)
- `METRICS_INFLUX_READ_TIMEOUT` — InfluxDB HTTP client read timeout in seconds (default: `60`)
- `METRICS_INFLUX_WRITE_TIMEOUT` — InfluxDB HTTP client write timeout in seconds (default: `60`)
- `METRICS_STORAGE_ROUTES_ANALYTICS_BUCKET` — InfluxDB 2 bucket for routes analytics data (default: `analytics-realtime`)
- `METRICS_STORAGE_ROUTES_ANALYTICS_MEASUREMENT` — InfluxDB 2 measurement name for routes analytics data (default: `routes_analytics`)
- `METRICS_STORAGE_ROUTES_ANALYTICS_TABLE` — InfluxDB 3 table name for routes analytics data (default: `routes_analytics`)

---

## 0.15.0

### Added

#### Security & RBAC

- `config.rest.security.default.roles-mapping` — JSON object mapping identity provider roles to application roles (`FULL_ADMIN`, `READ_ONLY_ADMIN`). Replaces `allowedRoles`
- `providers.*.roles-mapping` — per-provider role-to-application-role mapping; merged with default roles-mapping (provider takes priority on conflicts)
- `FULL_ADMIN` and `READ_ONLY_ADMIN` application roles — `READ_ONLY_ADMIN` users receive 403 on all mutating endpoints

#### Configuration Management

- `CORE_AUTH_TOKEN_PROVIDER_AUDIENCE` — audience (resource identifier) for the requested S2S authentication token; required by some OAuth2/OIDC providers (see [Auth0 S2S configuration](auth0-s2s-config.md))

#### Observability

- `METRICS_DATASOURCE_TYPE` — datasource type selector: `influx2` (default) or `influx3`
- `METRICS_INFLUX3_DEFAULT_PAGE_SIZE` — default page size for InfluxDB 3 queries (default: `100`)
- `METRICS_STORAGE_DATABASE` — InfluxDB 3 database name (default: `analytics-realtime`)
- `METRICS_STORAGE_ANALYTICS_BUCKET` — InfluxDB 2 bucket for analytics data (default: `analytics-realtime`)
- `METRICS_STORAGE_ANALYTICS_MEASUREMENT` — InfluxDB 2 measurement name for analytics data (default: `analytics`)
- `METRICS_STORAGE_MCP_ANALYTICS_BUCKET` — InfluxDB 2 bucket for MCP analytics data (default: `analytics-realtime`)
- `METRICS_STORAGE_MCP_ANALYTICS_MEASUREMENT` — InfluxDB 2 measurement name for MCP analytics data (default: `mcp_analytics`)
- `METRICS_STORAGE_ANALYTICS_TABLE` — InfluxDB 3 table name for analytics data (default: `analytics`)
- `METRICS_STORAGE_MCP_ANALYTICS_TABLE` — InfluxDB 3 table name for MCP analytics data (default: `mcp_analytics`)
- `METRICS_MAX_TIME_RANGE` — maximum query time range for dataset queries; human-readable duration format (default: `72h` for InfluxDB 3)

---

### Changed

#### Security & RBAC

- `config.rest.security.default.email-claim` (env: `CLAIMS_EMAIL_KEY`) — renamed from `config.rest.security.default.email.claims`; description updated to cover opaque token support (`/userinfo` response)
- `config.rest.security.default.principal-claim` (env: `SECURITY_USER_CLAIM`) — moved from `config.rest.security.principal-claim` to the default section; description updated for opaque token support

#### Observability

- `METRICS_CONFIG_CONTENT` — replaces `METRICS_CONFIGFILE_CONTENTENVVAR` for providing metrics configuration JSON content
- `METRICS_CONFIG_FILE` — replaces `METRICS_CONFIGFILE_LOCATION` for specifying the path to the metrics configuration file
- `METRICS_INFLUX2_DEFAULT_PAGE_SIZE` — renamed from `METRICS_DATASOURCE_INFLUX_DEFAULTPAGESIZE`
- `METRICS_STORAGE_ORG` — now applies only when using the default InfluxDB 2 configuration

---

### Deprecated

#### Security & RBAC

- `config.rest.security.default.allowedRoles` — replaced by `config.rest.security.default.roles-mapping`
- `providers.*.allowed-roles` — replaced by `providers.*.roles-mapping`

