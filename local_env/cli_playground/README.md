# CLI Playground

Quick hands-on examples for the `validate` CLI command.
All commands use Docker and mount the `configs/` directory from this folder.

## Setup

Build the image first (from the project root):

```bash
docker build -t epam/ai-dial-admin-backend:local .
```

Set a short alias for convenience:

```bash
IMAGE=epam/ai-dial-admin-backend:local
CONFIGS="$(pwd)/configs"
alias dial-validate='docker run --rm -v "$CONFIGS:/data" $IMAGE validate'
```

> All paths in the commands below use `/data/` — the container path where `configs/` is mounted.

---

## Demo 1 — Basic valid config (exit 0)

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate /data/01_basic/config.json
```

Expected output:
```json
{
  "status" : "valid",
  "strategy" : "MERGE_JSON",
  "files" : [ {
    "path" : "/data/01_basic/config.json",
    "status" : "valid"
  } ]
}
```

---

## Demo 2 — Sequential strategy: roles referenced from a later file

The second file references roles defined in the first. With SEQUENTIAL strategy, each file
is imported in order so cross-file references are resolved.

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --strategy SEQUENTIAL \
  /data/02_roles_and_models/roles.json \
  /data/02_roles_and_models/models.json
```

Swap the order to see it **fail** (model references roles that don't exist yet):

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --strategy SEQUENTIAL \
  /data/02_roles_and_models/models.json \
  /data/02_roles_and_models/roles.json
```

---

## Demo 3 — MERGE_JSON strategy: base + environment overrides

Two files are deep-merged before validation. The second file wins on conflicting keys
(here: `gpt-4.displayName` and `gpt-4.endpoint` are overridden by the prod overlay).

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --strategy MERGE_JSON \
  /data/03_merge/base.json \
  /data/03_merge/env-prod-overrides.json
```

---

## Demo 4 — Unknown property: warn vs fail

**Default (IGNORE) — exits 0, warning in output:**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  /data/05_invalid/unknown-field.json
```

**Strict (FAIL) — exits 1, error in output:**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --unknown-properties FAIL \
  /data/05_invalid/unknown-field.json
echo "Exit code: $?"
```

---

## Demo 5 — Core version schema check

`globalInterceptors` was added in Core 0.41.0. The config in `04_version_check/`
uses it. Checking against an older schema surfaces the incompatibility.

**Against latest schema (no warnings):**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version latest \
  /data/04_version_check/modern-config.json
```

**Against 0.37.0 schema (warning — field unknown to that version):**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version 0.37.0 \
  /data/04_version_check/modern-config.json
```

**Same, strict mode — exits 1 (CI-friendly gate):**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version 0.37.0 \
  --unknown-properties FAIL \
  /data/04_version_check/modern-config.json
echo "Exit code: $?"
```

**Pre-release version is normalised automatically:**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version 0.37.0-SNAPSHOT \
  /data/04_version_check/modern-config.json
```

---

## Demo 6 — Invalid JSON (exit 1)

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  /data/05_invalid/broken-json.json
echo "Exit code: $?"
```

---

## Demo 7 — Version argument errors (exit 2)

**Below minimum available schema:**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version 0.1.0 \
  /data/01_basic/config.json
echo "Exit code: $?"
```

**Bad version format:**
```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE validate \
  --core-config-version not-a-version \
  /data/01_basic/config.json
echo "Exit code: $?"
```

---

## Demo 8 — CI pipeline pattern

The command exits non-zero on any validation failure, making it safe to wire into CI:

```bash
docker run --rm -v "$CONFIGS:/data" $IMAGE \
  validate --unknown-properties FAIL /data/01_basic/config.json \
  && echo "Config is valid" \
  || (echo "Config is INVALID — blocking deployment"; exit 1)
```

---

## Exit codes reference

| Code | Meaning |
|------|---------|
| 0 | All files valid |
| 1 | Validation failed — JSON output on stdout with details |
| 2 | Bad argument (`--core-config-version` format/range) — message on stderr |
