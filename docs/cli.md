# DIAL Admin CLI

The DIAL Admin CLI is built into the `epam/ai-dial-admin-backend` Docker image. No separate installation is needed.

## Prerequisites

- Docker
- Config files accessible via a mounted volume

## Commands

### `validate`

Validates one or more DIAL Core JSON config files using the same import logic as the running service.

**Usage:**
```
docker run --rm \
  -v /path/to/your/configs:/data \
  epam/ai-dial-admin-backend:<tag> \
  validate [--strategy <MERGE_JSON|SEQUENTIAL>] [--conflict-resolution <OVERRIDE|SKIP>] <file1> [file2 ...]
```

**Options:**

| Option | Default | Description |
|--------|---------|-------------|
| `--strategy` | `MERGE_JSON` | How multiple files are combined before validation |
| `--conflict-resolution` | `OVERRIDE` | Conflict policy used for `SEQUENTIAL` strategy |
| `--help` | | Show command help |

**Strategies:**

- **`MERGE_JSON`** *(default)*: All files are deep-merged as JSON (later files win on conflicting keys), then validated as a single unit. Use this when your config is split across files that together form one complete configuration.

- **`SEQUENTIAL`**: Each file is imported independently in order. Later imports see entities created by earlier ones. Use this when files build on each other (e.g., `file2` references a role defined in `file1`).

## Exit Codes

| Code | Meaning |
|------|---------|
| `0` | All files are valid |
| `1` | One or more files are invalid — JSON output on stdout with details (includes file-not-found and JSON parse errors) |
| `2` | Unexpected internal error — message on stderr |

## Output Format

Output is always valid JSON on stdout.

**Success:**
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

**Failure:**
```json
{
  "status": "invalid",
  "strategy": "SEQUENTIAL",
  "files": [
    { "path": "/data/core.json", "status": "valid" },
    {
      "path": "/data/extra.json",
      "status": "invalid",
      "error": "Model 'gpt-4': upstream key 'prod-key' does not exist"
    }
  ]
}
```

## Examples

**Validate a single file:**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate /data/dial-core-config.json
```

**Validate multiple files merged together:**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate --strategy MERGE_JSON /data/base.json /data/env-overrides.json
```

**Validate files sequentially (cross-file references):**
```bash
docker run --rm -v $(pwd)/configs:/data \
  epam/ai-dial-admin-backend:latest \
  validate --strategy SEQUENTIAL /data/roles.json /data/models.json
```

**CI pipeline example (fail build on invalid config):**
```bash
docker run --rm -v $(pwd):/workspace \
  epam/ai-dial-admin-backend:latest \
  validate /workspace/config.json
echo "Exit code: $?"
```
