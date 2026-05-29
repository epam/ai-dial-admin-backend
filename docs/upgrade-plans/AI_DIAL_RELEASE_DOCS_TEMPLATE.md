# AI-DIAL Release Documentation Guidelines & Template

Based on the review standards established by the AI-DIAL maintainers, use the following rules and examples when creating or updating release notes and upgrade guides (`upgrade-to-X.XX.md`).

---

## 1. Standardized Configuration Tables

**Rule:** When documenting introduced features or new environment variables, you must use a standardized markdown table format with four exact columns: `Setting`, `Default`, `Required (Yes/No)`, and `Description`.

**Example:**
```markdown
### New Environment Variables

| Setting | Default | Required (Yes/No) | Description |
| :--- | :--- | :--- | :--- |
| `CLAIMS_EMAIL_KEY` | `email` | No | Specifies the key used to extract the email from the JWT claims. |
| `DIAL_ADMIN_URL` | `""` | Yes | The base URL for the DIAL Admin Panel frontend. |
```

---

## 2. Comprehensive Component Coverage

**Rule:** Do not omit component changes. If a component had pre-releases with changes (especially involving new or deprecated environment variables), those changes must be explicitly documented in the main release upgrade guide.

**Example:**
```markdown
### AI-DIAL Quickapps Backend (0.7.0-rc.1)
* Introduced new environment variables for database connection pooling.
* Deprecated `OLD_DB_URL` in favor of `QUICKAPPS_DB_URL`.
```

---

## 3. Clear Context and Use Cases

**Rule:** Do not just list new variables. Provide a clear statement and description of the specific use case explaining *when* and *why* a variable is required.

**Example:**
```markdown
**Note on `CLAIMS_EMAIL_KEY`:** 
You must add this environment variable if your Identity Provider (IdP) uses a custom claim field for the user's email address instead of the standard `email` claim. If your IdP uses the standard claim, you can leave this unset.
```

---

## 4. Explicit Incompatibility & Restriction Notes

**Rule:** Clearly highlight version mismatches, breaking changes, and feature limitations using GitHub-style alert blockquotes so they cannot be missed. Use the alert type that matches severity:

* `> [!NOTE]` — useful information the reader should know (e.g. changed validation behavior).
* `> [!IMPORTANT]` — required operator action when upgrading (e.g. mandatory config migration).
* `> [!CAUTION]` — risks and deprecations that still work but will break later.

**Example:**
```markdown
> [!IMPORTANT]
> DIAL Core `0.42.x` is **not compatible** with DIAL Admin Panel `0.16.0`. The Admin Panel produces a config with the `responsesDefaults` field, which is unknown to DIAL Core `0.42.x` and triggers config reload failures. You must upgrade DIAL Core to `0.43.0` before deploying the new Admin Panel.

> [!NOTE]
> Entity name validation has changed: the previous 160-character limit is replaced by a UTF-8 byte-based constraint, configurable via `NEXT_PUBLIC_RESOURCE_MAX_SEGMENT_BYTES`.
```

---

## 5. Link to Component-Level Documentation

**Rule:** The main release upgrade guide must aggregate and link to the individual component-level documentation, specifically the Upgrade Plans and Infrastructure Changelogs.

**Example:**
```markdown
### Detailed Component Instructions

For detailed component-level instructions, please refer to the following guides:
* [DIAL Admin Backend: Upgrade Plan - 0.16.0](https://github.com/epam/ai-dial-admin-backend/blob/release-0.16/docs/upgrade-plans/0.16.0.md)
* [DIAL Admin Backend: Infrastructure Changelog - 0.16.0](https://github.com/epam/ai-dial-admin-backend/blob/release-0.16/docs/INFRA-CHANGELOG.md#0160)
```

---

## 6. Deprecations, Migrations, and Removals

**Rule:** Document lifecycle changes to environment variables using one of the three standardized patterns below, depending on the situation. Always keep deprecated, migrated, and removed variables in **separate sub-sections** with their own heading, and never mix them into a single combined table.

### 6.1. Deprecated with a Replacement

When a variable is deprecated but a direct replacement exists, open the sub-section with a `> [!CAUTION]` alert stating that it still works but will be removed, then use a three-column table: `Variable`, `Replacement`, `Description`.

**Example:**
```markdown
#### Deprecated environment variables

> [!CAUTION]
> Still works, but will be removed in future versions.

| Variable | Replacement | Description |
| :--- | :--- | :--- |
| `PY_INTERPRETER_CLIENT_TIMEOUT` | `DEFAULT_TOOL_TIMEOUT_SECONDS` or `tool_defaults.timeout_seconds` | When set, still controls the PyInterpreter client timeout (default `60.0`); the unified tool-timeout settings are preferred. |
```

### 6.2. Deprecated with a State-Dependent Migration

When the required action depends on the operator's current configuration (i.e. there is no single replacement value), use a two-column `Variable`, `Description` table to declare the deprecation, followed by a separate **Migration:** table with `Previous configuration` and `Required action` columns.

**Example:**
```markdown
#### Deprecated environment variables

| Variable | Description |
| :--- | :--- |
| `DIAL_USE_FILE_STORAGE` | Deprecated. DIAL Storage is now enabled automatically when `DIAL_URL` is set, making this variable redundant. |

**Migration:**

| Previous configuration | Required action |
| :--- | :--- |
| `DIAL_URL` is set, `DIAL_USE_FILE_STORAGE` is `False` or unset | Remove `DIAL_URL` if you want to keep storage disabled |
| `DIAL_URL` is unset, `DIAL_USE_FILE_STORAGE=True` | No action needed; storage will be disabled (previously caused an error) |
| `DIAL_URL` is set, `DIAL_USE_FILE_STORAGE=True` | No changes required; behavior remains unchanged |
```

### 6.3. Removed Variables

When a variable has been fully removed, use a two-column `Variable`, `Description` table. The description must state that it was removed and explain the resulting behavior (the fallback/default that now applies).

**Example:**
```markdown
#### Removed environment variables

| Variable | Description |
| :--- | :--- |
| `MODEL_RATES` | Removed. Was used to calculate pricing for chat and embeddings requests when prompt logs were missing pricing information. DIAL Core has reliably provided pricing information in prompt logs since version 0.7.0 (Feb 2024), making this variable redundant. When pricing information is unavailable, the service now defaults to a price of `0.0`. |
```

---

## 7. Helm Chart Verification

**Rule:** Ensure that the specified versions of Helm charts have been explicitly tested against all components of the release before documenting them.

**Example:**
```markdown
### Helm Chart Updates

The following Helm charts have been verified and tested against all components in the 1.43 release:
* `dial-extension`: `3.0.1`
* `dial-core`: `2.5.0`
```