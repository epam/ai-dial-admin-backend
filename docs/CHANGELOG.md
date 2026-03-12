# Infra Task Changelog

Tracks infrastructure changes requested via GitHub Issues and completed by DevOps team.

---

## Format

Each entry corresponds to a GitHub Issue tagged `infra-task`.

```
## [YYYY-MM-DD] Issue #<number> — <short title> (<environment>)
- [x] task that was done — note if needed
- [ ] task that is pending or skipped — reason
```

---

<!-- New entries go below this line, newest first -->

## [2026-03-06] Example entry — Issue #0 (development)

> This is an example entry. Delete it once real tasks start coming in.

- [x] add DISABLE_OTEL=false (in EF section) — added to `application.yaml`, deployed to dev
- [x] set LOG_LEVEL=warn — updated via config map
