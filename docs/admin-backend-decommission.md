# Decommissioning admin-backend

This runbook is for DevOps teams that run DIAL and want to stop using **ai-dial-admin-backend**.

admin-backend keeps the DIAL Core configuration in its own database and, on a schedule, writes it out as Core's config file(s). Core loads those files. After the change, **admin-frontend talks to Core directly** and Core is the only place that holds configuration. Core serves traffic throughout and clients keep using the same names.

## What you need

| Component | Requirement |
|---|---|
| DIAL Core | 0.50.0 or later if you migrate config into blob storage (steps 4 and 6). The migrate endpoint exists since 0.48.0, but earlier releases miss fixes that affect the copied data (see below). Any version if you keep config files. |
| admin-frontend | 0.22 or later. From 0.22 it reads admin rights from Core. Core-only mode is on when `DIAL_ADMIN_API_URL` is not set. |
| Core admin rule | `access.admin.rules` in Core's settings must match your admins. Without it every caller is denied on the admin API. |

Why 0.50.0 and not 0.48.0, for the migration:

- **0.48.0** adds the endpoint and all 11 entity types.
- **0.49.0** adds fixes to what the migration copies: model and route upstream secrets, stray OAuth values in toolsets, duplicate-secret keys, entity name validation, and `rateLimitSchedule` in global settings.
- **0.50.0** adds `spendingLimits` to global settings. On 0.49 the migration does not copy spending limits.
- **0.51.0** adds the `routers` and `plugins` entity types, with migration support (see [Routers and plugins](#routers-and-plugins-core-0510-and-later)).

## Step 1. Choose how Core gets its configuration afterwards

Once admin-backend is gone, nothing generates the config files any more. Choose one of these.

| Path | What it means | Notes |
|---|---|---|
| **A. Move to blob storage** (recommended) | Core copies all file-defined entities into its blob storage. Core stops reading config files. | Entities are then edited in admin-frontend or through Core's API, at runtime, with no redeploy. |
| **B. Keep config files** | The config files Core loads today stay. You own them from now on. | You edit and deploy the files yourself. admin-frontend shows these entities read-only. |
| **C. Mix** | Migrate some entity types, keep others in files. | The migration accepts a list of types. If an entity exists in both, the blob copy wins. |

The steps below are marked with the paths they apply to. Steps without a mark apply to all paths.

## Step 2. Find out how Core gets its config today

1. On admin-backend, read `CONFIG_EXPORT_STORAGETYPE`:
   - `LOCAL_FILE`: one file at `CONFIG_EXPORT_OUTPUTFILE_PATH`, shared with Core through a volume.
   - `CONFIG_MAP`: one or more ConfigMaps (`CONFIG_EXPORT_CONFIGMAP_NAMES`, key `CONFIG_EXPORT_CONFIGMAP_KEY`).
   - `KUBE_SECRET`: one or more Secrets (`CONFIG_EXPORT_KUBESECRET_NAMES`, key `CONFIG_EXPORT_KUBESECRET_KEY`).
   - If `CONFIG_EXPORT_KEYVAULT_TYPE` is not `none`, secrets (API keys, upstream keys, client secrets) are split into a key vault and mounted into Core as separate files.
2. On Core, read `aidial.config.files` and list every file. Mark which ones admin-backend writes (the export file, ConfigMaps, Secrets, vault secrets) and which ones people maintain by hand.
3. Record the answer. You need it in steps 7 and 9.

## Step 3. Get admin credentials and a baseline

All Core calls below need a caller that matches `access.admin.rules`. Use one option.

- **Option A: admin API key.** Use a Core key whose `roles` satisfy the admin rule (with the default rule, the role `admin`). Core checks the rule against the key's roles and reads the key from the `API-KEY` header. The key admin-backend uses to call Core (`CORE_AUTH_API_KEY_VALUE`) works if it has that role.
- **Option B: user access token.** Sign in as a user who matches the admin rule and use their access token in the `Authorization: Bearer` header.

```bash
export CORE=https://<core-host>

# Option A: admin API key
export AUTH_HEADER="API-KEY: <admin api key>"
# Option B: user access token
# export AUTH_HEADER="Authorization: Bearer <admin access token>"

curl -sS "$CORE/v1/user/role-assignments" -H "$AUTH_HEADER"   # expect "adminAccess": "FULL"
curl -sS "$CORE/openai/models"       -H "$AUTH_HEADER" | jq -r '.data[].id' | sort > before-models.txt
curl -sS "$CORE/openai/applications" -H "$AUTH_HEADER" | jq -r '.data[].id' | sort > before-apps.txt
```

**Done when** role-assignments returns `FULL` and both baseline files are saved.

Also take the backups described in [Back up first](#back-up-first) now, before step 4.

If you use the admin-backend key, keep it until the last step. The migration copies it to blob storage, so it keeps working after Core stops reading files.

## Step 4. Dry run of the migration (paths A and C)

admin-backend stays live. Nothing is written.

```bash
curl -sS -X POST "$CORE/v1/admin/config/file/migrate" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d '{"dryRun": true}' | tee migrate-dry-run.json \
  | jq -r '.results | group_by(.status)[] | "\(.[0].status): \(length)"'

jq '.results[] | select(.status=="would_fail")' migrate-dry-run.json
```

For path C, pass the types you want, for example `{"dryRun": true, "types": ["models", "interceptors"]}`. Supported types: `settings`, `schemas`, `catalog_schemas`, `interceptors`, `routers`, `translators`, `plugins`, `roles`, `keys`, `routes`, `models`, `toolsets`, `applications`. `routers` and `plugins` exist only on Core 0.51.0 and later. Leave `types` out to cover all of them.

The migration works on everything Core merged from all its config files, whoever writes them.

Fix each `would_fail` at its source: in admin-backend for its entities, in your own config files for hand-maintained ones. Wait for the next export and Core reload (about a minute), then run the dry run again.

**Done when** every result is `would_migrate` or `would_skip`, or you have agreed to drop the entities that still fail.

## Step 5. Freeze configuration edits

1. Announce a freeze: no edits in the admin-backend UI and none in hand-maintained config files.
2. Wait one export period (`CONFIG_EXPORT_SYNCPERIOD`, 15 seconds by default) so the last edits reach Core.
3. Set `CONFIG_EXPORT_ENABLED=false` on admin-backend and restart it. The exported ConfigMaps, Secrets and vault secrets keep their last values.

This step matters on paths A and C because, once an entity is in blob storage, Core ignores its file copy. Edits made in admin-backend after the migration are silently lost. On path B it freezes the last exported version, which you keep in step 7.

**Done when** admin-backend runs with export disabled and the freeze is announced.

## Step 6. Run the migration (paths A and C)

It takes effect immediately and needs no restart.

```bash
curl -sS -X POST "$CORE/v1/admin/config/file/migrate" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d '{}' | tee migrate.json \
  | jq -r '.results | group_by(.status)[] | "\(.[0].status): \(length)"'

jq '.results[] | select(.status=="failed")' migrate.json
jq '.results[] | select(.kind=="Settings")' migrate.json
```

It is safe to run again. Entities already in blob storage are reported `skipped` and never overwritten.

Check these points:

- **Settings.** `settings` covers global interceptors, retriable error codes, the rate limit schedule and spending limits. If the result is `skipped`, someone already saved System Properties in admin-frontend. Core then uses those values and ignores the files' values. If it is `migrated`, check the values in System Properties.
- **Global interceptors from admin-backend.** From reading admin-backend's code (not tested), its split export (`CONFIG_MAP`, `KUBE_SECRET`, or any key vault type) leaves out `globalInterceptors`. In that setup the global interceptors set in admin-backend never reached Core. Compare `GET /api/v1/global-settings` on admin-backend with Core's System Properties before step 9. Adding them in Core turns them on for every request for the first time, so the owners decide.
- **Not migrated.** The endpoint has no type for `orgUnitRoles`. If your config files define them, keep a small config file with only those (see step 7).
- **Behaviour.**

```bash
curl -sS "$CORE/openai/models"       -H "$AUTH_HEADER" | jq -r '.data[].id' | sort | diff before-models.txt -
curl -sS "$CORE/openai/applications" -H "$AUTH_HEADER" | jq -r '.data[].id' | sort | diff before-apps.txt -
```

**Done when** every result is `migrated` or `skipped`, both diffs are empty, and one real chat request with an existing API key works.

### Routers and plugins (Core 0.51.0 and later)

Core 0.51.0 adds two entity types, and the migration covers both:

- **Routers** (`routers`) are deployments that choose which deployment serves a request.
- **Plugins** (`plugins`) are plugin instances that Core runs in process. Each one names a plugin `type` and holds its `settings`.

What to know:

- **admin-backend does not manage them.** Its bundled config schemas stop at Core 0.50.0, so it neither stores nor exports routers and plugins. If you have any, they are in a config file that someone maintains by hand. They are not lost when admin-backend is removed.
- **The migration orders them correctly.** Plugins are written before the routers, models and applications that may use them.
- **Plugin code is not migrated.** A plugin's `type` refers to code that Core loads at start from its plugin directory or image. The migration moves only the instance entries. Keep the plugin jars in Core's image or volume.
- **Plugin settings hold no secrets.** Core stores and returns them as written.
- **admin-frontend has no page for them.** In the frontend version we checked, there is no page for routers or plugins, whether the entries are in files or in blob storage. After the migration you manage them through Core's API (`/v1/routers/platform/{name}`, `/v1/plugins/platform/{name}`).
- **Path B or C:** if you keep them in a file, that file is covered by step 7 like any other.

## Step 7. Settle Core's config sources

### Path A: stop Core reading config files (one Core rollout)

1. Set `aidial.config.files` to `[]`. If you keep a file for `orgUnitRoles`, list only that one.
2. In the same rollout, remove the mounts of the files you no longer list.

Core refuses to start if a file in `aidial.config.files` is missing. Change the setting before or together with removing mounts, never after.

Repeat the behaviour checks from step 6. Nothing should change, because the blob copies already take precedence. If something disappears, it was not migrated: put the files back, migrate it, and try again.

### Paths B and C: take over the remaining files

The ConfigMaps, Secrets, volume and vault secrets that admin-backend wrote belong to its deployment. Step 9 deletes them, and Core would then fail to start.

1. For every file listed in `aidial.config.files` that admin-backend writes, copy its current content into a ConfigMap, Secret or volume that you own and that nothing else writes. Include the vault secrets if your export used a key vault.
2. Point the mounts and `aidial.config.files` at your copies.
3. Roll out Core and repeat the behaviour checks from step 6.
4. From now on, you edit these files and deploy them yourself.

For path C, also delete from your files the entities that you migrated. Leaving them in both places is allowed, because the blob copy wins. But if the two copies drift apart, credentials and rate-limit counters for that name can be attributed to the wrong definition.

**Done when** Core pods are healthy, both diffs are still empty, and the chat request still works.

## Step 8. Switch admin-frontend to Core only

In the admin-frontend environment (in the dial-admin Helm chart: `frontend.env`):

- Delete the `DIAL_ADMIN_API_URL` key entirely. An empty value does not work: menus would show pages that redirect to Home.
- Keep `DIAL_CORE_API_URL`, `NEXTAUTH_SECRET`, `NEXTAUTH_URL` and your identity provider variables.
- Optional cleanup, unused without admin-backend: `ENABLE_EXPORT_CONFIG_MAP`, `GRAFANA_LINK`, `ADMIN_ROLE_NAMES`, `DIAL_ROLES_FIELD`, `AUTH_*_ADMIN_ROLE_NAMES`, `AUTH_*_DIAL_ROLES_FIELD`.
- Optional partial replacement for usage and audit data: `ANALYTICS_ENABLED=true`, `ANALYTICS_USAGE_ENABLED=true`, `DIAL_ANALYTICS_API_URL`.

**Done when** the Entities, Builders, Access Management and Audit menus are gone. The AI Gateway and Governance pages list your entities. On path A you can also edit and save one model. On path B entities defined in files are read-only in the UI.

## Step 9. Remove admin-backend

After a soak period of about a week, and keeping the step backups:

1. Scale admin-backend to 0.
2. Delete its deployment, service, ingress, database, volumes, service account and its RBAC on ConfigMaps and Secrets.
3. Delete the ConfigMaps and Secrets it exported to, and the key vault secrets it wrote. On paths B and C only after step 7 moved Core onto your own copies.
4. If admin-backend called Core with an API key (`CORE_AUTH_API_KEY_VALUE`), delete that key in admin-frontend under Governance → Keys.

**Done when** nothing in the namespace or the key vault belongs to admin-backend.

## Back up first

Do this before step 4 (it is also your rollback material):

- Dump admin-backend's database (H2 file, PostgreSQL or SQL Server). It is the only copy of entity history, the activity audit, adapters, interceptor runners, topics, and entities that admin-backend never exported (invalid ones, addons, assistants).
- Copy every config file Core loads. If you use a key vault, store the copy as a secret, because the files hold API keys and upstream secrets.

## Rollback

Until step 9 you can return to the old setup.

- Restore Core's `aidial.config.files` and the mounts, set `DIAL_ADMIN_API_URL` on the frontend again, and set `CONFIG_EXPORT_ENABLED=true` on admin-backend.
- If you migrated: blob copies keep taking precedence over files. For admin-backend or your files to control an entity again, delete its blob copy with `DELETE $CORE/v1/{type}/platform/{name}`. Edits made to the blob copy after the migration are lost.

## What goes away with admin-backend

Core has no replacement for these:

| Feature | After the change |
|---|---|
| Entity history, rollback, activity audit | Gone. The data stays only in the database dump. |
| Config import/export (the Configuration scope) | Gone. Asset-level import/export in admin-frontend still works. |
| Telemetry dashboards, usage log | Gone. The analytics variables in step 8 are a partial replacement. |
| Adapters, Interceptor Runners, Interceptor Templates | Gone. Migrated models and interceptors keep the concrete endpoints admin-backend had resolved. |
| Container endpoint refresh from Deployment Manager | Gone. If a container's URL changes, update the entity by hand. |
| Topics on keys, roles, routes, interceptors | Gone. Topics on models, applications and toolsets carry over. |
