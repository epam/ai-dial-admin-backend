# DIAL Admin CLI Tool — Design

**Date:** 2026-03-05
**Status:** Approved

## Overview

Add a CLI mode to the existing `ai-dial-admin-backend` Docker image. The first command is `validate`, which verifies one or more DIAL Core format JSON config files using the same import logic as the running service. Future commands (e.g. `create-model`, `export`) will follow the same pattern. The CLI is intended for DevOps pipeline hooks — no UI, no running server required.

---

## Section 1: Architecture & Mode Switching

### Entry point

`Application.main()` inspects `args`. If the first argument matches a known CLI command, the app starts in CLI mode before the Spring context loads:

- `WebApplicationType.NONE` — Tomcat does not start
- Spring profile `cli` activated
- `DatasourceVendorValidator` skipped (in-memory H2 is always used in CLI mode)

If no args are present the app starts normally as a web server. Existing deployments are unaffected.

### `cli` Spring profile (`application-cli.properties`)

| Property | Value |
|----------|-------|
| Datasource | H2 in-memory (`jdbc:h2:mem:cli-validation`) |
| Flyway | Runs migrations on in-memory DB (~1s) |
| `config.export.enabled` | `false` |
| `config.import.autoImportOnBootstrap.enabled` | `false` |
| OIDC/Keycloak auth beans | Disabled |

### Picocli wiring

A `CliApplicationRunner` (`ApplicationRunner`, active only under `cli` profile) calls `CommandLine.execute(args)` and `System.exit()` with the result code. The root command is `DialAdminCommand`. Subcommands are Spring beans — picocli's Spring `IFactory` resolves `@Autowired` dependencies inside them.

### Docker usage

```bash
# Server mode (unchanged)
docker run epam/ai-dial-admin-backend:tag

# CLI mode
docker run -v ./configs:/data epam/ai-dial-admin-backend:tag validate /data/core.json /data/extra.json
```

---

## Section 2: `validate` subcommand

### Command signature

```
dial-admin validate <file1> [file2 ...] [--strategy <MERGE_JSON|SEQUENTIAL>] [--conflict-resolution <OVERRIDE|SKIP>]
```

- `@Parameters(arity = "1..*")` — one or more absolute/relative paths to DIAL Core JSON config files
- `--strategy` — multi-file handling strategy (default: `MERGE_JSON`)
- `--conflict-resolution` — conflict policy passed to the importer (default: `OVERRIDE`, used by `SEQUENTIAL` strategy)

### Validation flow

Uses **real import** (not `importPreview`) against the ephemeral in-memory H2 DB. Since the JVM process exits after the command, committing vs rolling back has identical end-state. Real import is preferred because:

- Simpler code path (no `setRollbackOnly()` trick)
- For `SEQUENTIAL` strategy, each committed import is visible to subsequent files (enabling cross-file entity references)
- Exercises the exact same code path as the running service

Each file is validated and reported individually in the output.

---

## Section 3: Multi-file strategies

Both strategies apply to the CLI `validate` command **and** to the auto-import-on-bootstrap service.

### `MERGE_JSON` (default)

1. Read all files as raw JSON strings
2. Deep-merge in list order — later file wins on key conflict at every nesting level
3. Deserialize merged result once as `Config`
4. One `importConfig` call

Reuses existing `ConfigMerger` logic (already used by `CompositeConfigSource` for K8s multi-part configs).

### `SEQUENTIAL`

1. Deserialize each file independently as `Config`
2. Import one-by-one using the configured `ConflictResolutionPolicy`
3. Each import commits — later files see entities created by earlier files
4. First failure stops the sequence; error is reported with the failing file path

### Auto-import property changes

```properties
# Existing (unchanged behaviour)
config.import.autoImportOnBootstrap.enabled=true

# New properties
config.import.autoImportOnBootstrap.strategy=MERGE_JSON
config.import.autoImportOnBootstrap.filePaths=/data/core.json,/data/extra.json
```

Default strategy `MERGE_JSON` keeps existing single-file deployments backward-compatible (single entry in `filePaths` behaves identically to before).

---

## Section 4: Output format & exit codes

### Stdout (JSON always)

**Success (exit 0):**
```json
{
  "status": "valid",
  "strategy": "MERGE_JSON",
  "files": [
    { "path": "/data/core.json", "status": "valid" },
    { "path": "/data/extra.json", "status": "valid" }
  ]
}
```

**Failure (exit 1):**
```json
{
  "status": "invalid",
  "strategy": "SEQUENTIAL",
  "files": [
    { "path": "/data/core.json", "status": "valid" },
    { "path": "/data/extra.json", "status": "invalid", "error": "Model 'gpt-4o': upstream key 'prod-key' does not exist" }
  ]
}
```

### Exit codes

| Code | Meaning |
|------|---------|
| `0` | All files valid |
| `1` | At least one file invalid (expected failure, valid JSON on stdout) |
| `2` | Unexpected error (file not found, DB init failure, etc.) — message on stderr |

### Error message mapping

| Exception | Output format |
|-----------|---------------|
| `JsonParseException` | `Invalid JSON at line N: <message>` |
| `JsonMappingException` | `Invalid field '<path>': <message>` |
| `ValidationException` | message as-is |
| `EntityAlreadyExistsException` | message as-is |
| `SchemaValidationException` | message as-is |
| Other `RuntimeException` | `Unexpected error: <message>` → exit 2 |

---

## Section 5: Testing

### Unit tests

- `ValidateCommand` — mock `ConfigImporter`, assert JSON output and exit code for each exception type
- `JsonMerger` (new utility) — deep-merge ordering (later file wins), empty list, single file passthrough
- Error message mapping — each exception type maps to the correct format

### Integration tests (Spring `cli` profile, in-memory H2)

- Valid single file → exit 0, `status: valid`
- Valid multi-file `MERGE_JSON` — second file overrides a field from first → import succeeds
- Valid multi-file `SEQUENTIAL` — second file references a role from first → import succeeds
- Invalid file (bad JSON) → exit 1, error contains line number
- Invalid file (duplicate model name) → exit 1, descriptive error
- Invalid file (schema violation) → exit 1, descriptive error
- File not found → exit 2
- `SEQUENTIAL`: first file fails → second file not attempted, single error reported

### Existing auto-import tests

Extend `CoreConfigAutoImportOnBootstrapFunctionalTest` with multi-file scenarios for both strategies.

---

## Section 6: CLI usage guide

A `docs/cli.md` file is generated as part of this feature covering:

- Prerequisites (Docker image tag, volume mounting)
- Command reference with all flags and defaults
- Example pipeline hook snippets (GitHub Actions, GitLab CI)
- Auto-import multi-file configuration reference
- Exit code table for CI integration
