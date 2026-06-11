---
name: admin-backend-release-notes
description: Use when the user asks to enhance, refine, polish, or "look at" the release notes for a tag — typically a fresh CI-generated pre-release (e.g. `0.17.0-rc.0`) or a stable cut. Reads the auto-generated notes off the GitHub release, classifies and rewrites each bullet in this project's editorial voice, builds BREAKING CHANGES sections from `docs/INFRA-CHANGELOG.md` / PR bodies / source code, and saves a draft to `claude/release-notes/`. Never edits GitHub directly.
allowed-tools: Read Grep Glob LSP Bash(gh release view:*) Bash(gh release list:*) Bash(gh pr view:*) Bash(gh pr list:*) Bash(gh pr diff:*) Bash(git log:*) Bash(git show:*) Bash(git diff:*) Bash(git tag:*) Bash(git rev-parse:*) Bash(date:*) Write(claude/release-notes/*) Bash(mkdir -p claude/release-notes)
argument-hint: "[tag]"
arguments: tag
model: opus
effort: xhigh
context: fork
agent: general-purpose
---

# Release-notes enhancer

The CI publishes a release for every tag with bullets that are just the PR titles. Those bullets carry a lot of dirt — conventional-commit prefixes (`feat:`, `fix(area):`), branch slugs (`async-dial-instead-of-dial-core-client`), misfiled items (a `feat(file-loader)` landing under `Other` because the title started with `feat(`), and a `## Other` section that mixes consumer-relevant items with pure internal refactors. The releases visible at `https://github.com/epam/ai-dial-admin-backend/releases/tag/0.15.0`, `https://github.com/epam/ai-dial-admin-backend/releases/tag/0.16.0-rc.0`, `https://github.com/epam/ai-dial-admin-backend/releases/tag/0.16.0` are what those raw notes look like after a human editorial pass. This skill reproduces that pass.

You are running in a forked, isolated context. Read and research freely — only the final summary you return reaches the main conversation. All file writes happen in this fork; the draft lands at `claude/release-notes/<tag>-draft.md`.

## When to use

- "Enhance the release notes for `0.8.0-rc.1`"
- "Look at the latest pre-release notes and refine them"
- "Help me adjust release notes for the current rc"
- "The CI just published `<tag>`, make it readable"

Do **not** trigger on requests like "what changed in 0.7.0?" — that is a recall question, not a notes-editing task.

## Inputs

`tag` = `$tag` — the GitHub release tag to enhance (e.g. `0.8.0-rc.1`, `0.9.0`). If empty, pick the most recent tag from `gh release list --limit 5` and confirm with the user before editing.

## Workflow

### 1. Resolve target and reference styles

1. `gh release view <tag> --json body,name,tagName` — capture the raw CI notes.
2. `gh release list --limit 10` — locate the previous tag of the same kind (last stable for a stable release, the predecessor `rc` for a delta `rc.N+1`).
3. `gh release view <prev-stable-tag> --json body` and `gh release view <prev-rc-tag> --json body` (when relevant) — these are the style anchors. The user has repeatedly insisted **"keep the same format as the latest stable release"** and **"your notes are too verbose"** — match the terseness of those notes, not your own instincts. One line per bullet.
4. `git tag --list | sort -V` + `git log <prev-tag>..<tag> --oneline` — full commit list for the range, so you can spot hotfix commits the CI dropped because they had no PR.
5. **Identify intermediate hotfix releases.** From the tag list, find any patch/hotfix releases that were published between `<prev-stable>` and the current tag (e.g. `0.16.1` between `0.16.0` and `0.17.0-rc.0`). For each such hotfix tag, collect its PR numbers via `gh release view <hotfix-tag> --json body` — these are changes that consumers already received. Record this set; it is used in Step 4 to drop re-appearing entries.
6. Construct the advisory header URLs — always use `blob/<tag>/docs/...` (not branch-based). For the upgrade-plan path, strip any `-rc.N` suffix to get the base version: `0.17.0-rc.0` → `docs/upgrade-plans/0.17.0.md`.

### 2. Pull source context for each bullet

For every bullet in the raw notes:

1. Parse out the trailing `(#<PR>)`. That PR number is the canonical starting point.
2. `gh pr view <PR> --json title,body,labels` — read the PR body, not just the title. The body is where the *what* and the *context* live; the title is usually too compressed.
3. **Extract the issue number from the PR body.** Search the body for `Closes #N`, `Fixes #N`, `Resolves #N`, `Related to #N`, or a GitHub issue URL (`github.com/epam/ai-dial-admin-backend/issues/N` or a cross-repo URL). That `N` is the leading issue reference for the bullet. If the issue is in another repo (e.g., `epam/ai-dial-admin-frontend#N`), use the full `repo#N` form. If no issue reference is found in the PR body, omit the leading issue link entirely — the bullet starts directly with the description and ends with `([#PR](url))`.
4. **Source-of-truth precedence** — in order: (a) PR description, (b) `gh pr diff <PR>` when the description is thin or vague, (c) source code read directly. If the PR body names something differently from the code (a different env-var name, endpoint path, or config field), **source code wins**. Never copy a wrong name from a PR description.
5. For bullets without a PR number (`* fix tests`, `* Merge remote-tracking branch ...`), find the commit with `git log <prev-tag>..<tag> --oneline | grep -i <keywords>` and `git show <hash>` — these are usually hotfix commits that should fold into a related entry, not stand alone.
6. If a PR body references a doc under `docs/designs/` or `docs/`, skim it for the headline framing.

### 3. Cross-check `README.md` / `CONFIGURATION.md` / source for config changes

- `git diff <prev-tag>..<tag> -- README.md docs/configuration.md` — env-var additions/removals/renames.
- For any env var mentioned in PR bodies or commit messages, verify the **canonical name** by reading the actual settings class via LSP (`goToDefinition` on the field, or `Grep` the codebase). The PR body said one thing, the code said another, code wins.
- Confirm defaults and bounds by reading the settings class.

### 3a. Cross-link `docs/INFRA-CHANGELOG.md` entries

1. `git diff <prev-tag>..<tag> -- docs/INFRA-CHANGELOG.md` (or `cat docs/INFRA-CHANGELOG.md`) — identify which sections were added/changed/deprecated in this range.
2. For each INFRA-CHANGELOG entry, find the corresponding bullet in the commit range (by PR number or keyword match).
3. Append a second line to that bullet:
   ```
       See [Section Name](https://github.com/epam/ai-dial-admin-backend/blob/<tag>/docs/INFRA-CHANGELOG.md#anchor)
   ```
   Use the exact section heading as the link text and the GitHub anchor fragment (lowercase, spaces→hyphens, punctuation stripped).
4. Use the INFRA-CHANGELOG entry type to guide classification in Step 6:
   - "Changed" or "Deprecated" entries that require operator action → BREAKING CHANGES (configuration) candidate.
   - "Added" entries that extend existing config → stays in Features.

### 4. Classify each bullet (move things between sections, drop the noise)

The raw notes' `## Features` / `## Fixes` / `## Other` partition is unreliable because CI keys it off the conventional-commit prefix in the PR title. Reclassify by the change's actual user impact:

| Where CI put it                                        | Where it belongs                                                    | Rule                                                                                                     |
|--------------------------------------------------------|---------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `Other` starting with `feat(...)`                      | `Features`                                                          | A feat that lost its slot to a scope prefix.                                                             |
| `Other` starting with `fix(...)`                       | `Fixes`                                                             | Same, for fix.                                                                                           |
| `Features` or `Fixes`, PR title starts with `feat:`/`fix:`, **no issue in PR body**, and the diff adds **no consumer-visible change** (new endpoint, new env var, new config field, changed behavior) | `Other` | Likely a docs or internal update with a misleading conventional prefix. |
| `Features` or `Fixes`, PR title starts with `feat:`/`fix:`, **no issue in PR body**, but the diff **does** add a consumer-visible change | Keep in `Features`/`Fixes`; flag the missing issue in the editorial notes | The absent issue link doesn't override actual consumer impact — classify by what the change does, not by what ticket it cites. |
| Any section, PR title starts with `chore:` | `Other` | Chore PRs are maintenance by definition; stay in Other even if the diff touches consumer-visible code. |
| `Features` / `Fixes` for a pre-release-only regression | `Fixes` with note "(affects pre-release users of \<feature\> only)" | Don't surface a transient bug as a feature.                                                              |
| `Other` for a security CVE bump                        | `Fixes`                                                             | Security items are user-relevant.                                                                        |
| Multiple PRs / hotfix commits on one feature           | one folded entry under the appropriate section — **only when all PRs address the same technical topic** (e.g. a feature PR followed by QA-found fix PRs for that same feature). If multiple PRs share a GitHub issue but each covers a different asset or subsystem, keep them as separate bullets. | Cite the commit hashes or PR numbers in parens.                                                          |

**Drop these from the notes entirely** — they have zero signal for any reader:

- `Merge remote-tracking branch …` commits.
- Pure Claude Code / skill scaffolding entries (`init claude documentation`, `add design review skill`).
- **PRs already shipped in an intermediate hotfix release** (identified in Step 1.5). Even though their commits may not be ancestors of the hotfix tag in the Git graph (causing CI to surface them again), they are not new to consumers. Drop them and note each one in the editorial-notes file with reason "already released in `<hotfix-tag>`".

**Keep in `Other`** — the actual polished releases retain all of these:

- Dependency bumps (security-adjacent or routine: `bump org.springframework`, `bump tomcat-embed-core`, etc.).
- Integration-test and unit-test improvements (`improve integration tests performance`).
- Internal refactors that touch observable subsystems (`refactor config filtering against core version`).
- CI/workflow improvements (`add release candidate branching`, `upgrades ci flows`).
- Multi-aggregate or query improvements (`multi-agg branch collapse, groupBy alias`).
- Documentation PRs (`add google docs`, `add infra tasks and changelog`).
- Auth-handling checks and issue templates (visible to contributors).

If you find yourself unsure whether to drop a bullet from `Other`, keep it — the polished reference releases err on the side of inclusion for maintainer-relevant items.

### 5. Rewrite each kept bullet

The raw form is `* <conventional-prefix>: <description> (#<PR>)`. Rewrite to:

```
*   [#ISSUE](https://github.com/epam/ai-dial-admin-backend/issues/ISSUE) Description text ([#PR](https://github.com/epam/ai-dial-admin-backend/pull/PR))
```

For cross-repo issues: `[epam/ai-dial-admin-frontend#N](url)`.
For no issue: `*   Description ([#PR](url))`.
Multiple issues: `[#N1](url), [#N2](url) Description ([#PR1](url), [#PR2](url))`.

Rules in order of importance:

1. **One line per bullet.** No multi-paragraph descriptions. If you need more detail, save it to the companion editorial-notes file (see §8), not the main draft.
2. **Drop the conventional prefix** (`feat:`, `fix:`, `chore:`, `fix(area):`). Replace with prose.
3. **Drop branch-style slugs.** The PR title is the prompt, not the output.
4. **Direct active voice.** Write a clear declarative sentence. Use a colon to enumerate specific identifiers when needed. **No em-dash "why" clause.**
5. **Backticks for code identifiers**: env vars, file paths, config field names, class names, endpoint paths, schema keys.
6. **Issue ref at the start, PR ref(s) at the end.** See format above. Extract the issue number from the PR body (see Step 2, point 3).
7. **Flag regressions explicitly**: `(regression fix)` for items restoring previously-working behavior.
8. **Quote CVE IDs verbatim** for security upgrades.

#### Example transformations

Each pair is `raw CI → enhanced`. Backticks in the enhanced form are literal.

```
# Dropping `feat:`, adding issue ref, colon-enumeration for details:
- * feat: add role based access control (#761)
+ *   [#681](url) Added role based access control: introduced `READ_ONLY_ADMIN` and `FULL_ADMIN` application roles ([#761](url))

# Dropping `fix:`, restoring issue ref from PR body:
- * fix: resource path double encoding (#715)
+ *   [#714](url) Fixed resource path double encoding ([#715](url))

# Multiple issues, multiple PRs:
- * improve activity audit and system rollback (#719) (#732) (#767) (#780)
+ *   [#595](url), [#684](url), [#700](url), [#743](url) Improved activity audit and system rollback ([#719](url), [#732](url), [#767](url), [#780](url))

# Cross-repo issue:
- * add redirectUrl field into toolset sign-in request body (#809)
+ *   [epam/ai-dial-admin-frontend#2756](url) Added `redirectUrl` field into toolset `/sign-in` request body ([#809](url))

# Feature with INFRA-CHANGELOG cross-link:
- * feat: make InfluxDB HTTP client timeouts configurable (#870)
+ *   [#773](url) Made InfluxDB HTTP client timeouts configurable ([#870](url))
+     See [Added Observability](https://github.com/epam/ai-dial-admin-backend/blob/<tag>/docs/INFRA-CHANGELOG.md#observability)

# Regression fix:
- * fix: restore bearer token forwarding (#250)
+ *   [#249](url) Restored bearer-token forwarding for DIAL deployment tools (regression fix) ([#250](url))

# Orphan hotfix commit folded into a related fix:
- * application mcp endpoint path     (orphan commit, no PR)
+ *   [#936](url) Adjusted Core MCP endpoint path for applications (`eb664c0`) ([#934](url))
```

### 6. Classify BREAKING CHANGES

Decide which bullets (if any) belong in BREAKING CHANGES sections. These sections sit above Features and are the only way to signal compatibility-breaking changes to operators and API consumers.

**BREAKING CHANGES (configuration)** — the operator must change something *outside* this service to keep it working after upgrade. Indicators:
- An env var was renamed, removed, or its semantics changed.
- The INFRA-CHANGELOG has a "Changed" or "Deprecated" entry that requires a manual migration step.

Keep entries to one line. Append `See [Section](url)` on the next line when the INFRA-CHANGELOG has the matching section (see Step 3a).

**BREAKING CHANGES (API)** — an existing API contract changes in a way that breaks callers. Indicators:
- An endpoint is removed, renamed, or its request/response schema changes incompatibly.
- A new mandatory field or enum value is introduced that breaks exhaustive switch statements or strict validators in client code (e.g., a new `sourceType` value that existing parsers reject).

New-only additions that don't alter existing contracts stay in Features, not here.

**A bullet can appear in only one section.** If a change is both an API break and requires config migration, place it in BREAKING CHANGES (configuration) and note the API impact in the description.

Omit both sections entirely when there are no breaking changes in the range.

### 7. Pre-release / delta handling

If the target is `<X.Y.Z>-rc.N` with `N ≥ 1`:

- The release covers only what changed since the previous rc — do **not** consolidate or rewrite the predecessor's notes. Each pre-release tag has its own GitHub release page; the consolidation happens at the stable cut.
- Drop sections that have no entries in the delta (e.g. omit BREAKING CHANGES sections if none apply).
- Do **not** prepend a "Delta since <prev-rc>" pointer at the top. The CI doesn't emit one, the previously-shipped rc notes don't carry one, and the `-rc.N` version suffix already signals what the release is. Adding a header just creates editorial noise the user has to clean up.

### 8. Save the draft (and optional editorial companion)

Create `claude/release-notes/` if missing, then write:

- **`claude/release-notes/<tag>-draft.md`** — the final notes, ready to paste into the GitHub release body. No preamble, no commentary — just the headings and bullets.
- **`claude/release-notes/<tag>-editorial-notes.md`** *(optional)* — only when there are non-obvious calls worth surfacing to the user:
    - Rename mapping (raw bullet → enhanced bullet) for items where the rewrite is non-trivial.
    - List of items dropped, with one-line reason per item.
    - Open questions for the user (e.g. "Should `#236` CI workflow PR stay under Other? It's invisible to consumers but visible to release maintainers.").
    - Any place the source-of-truth diverged from the PR body (e.g. canonical env-var name).

### 9. Verify nothing was pushed to GitHub

This skill **never** runs `gh release edit`, `gh release create`, or any write operation against the repo. The user explicitly directed: *"Everything should be drafted in local files. Don't push anything or change anything in GitHub."* Draft files are the only output. If the user later asks you to apply, that is a separate, explicit request.

## Output format

The file saved to `claude/release-notes/<tag>-draft.md` follows this shape exactly:

```markdown
UPGRADE TO NEW RELEASE
----------------------

**Please review [upgrade plan](https://github.com/epam/ai-dial-admin-backend/blob/<tag>/docs/upgrade-plans/<version>.md) before new release installation.**

INFRASTRUCTURE CHANGELOG
------------------------

**Please review [infrastructure changelog](https://github.com/epam/ai-dial-admin-backend/blob/<tag>/docs/INFRA-CHANGELOG.md) before new release installation.**

BREAKING CHANGES (configuration)
---------------------------------

*   [#ISSUE](url) Description ([#PR](url))
    See [Section Name](https://github.com/epam/ai-dial-admin-backend/blob/<tag>/docs/INFRA-CHANGELOG.md#anchor)

BREAKING CHANGES (API)
----------------------

*   [#ISSUE](url) Description ([#PR](url))

Features
--------

*   [#ISSUE](url) Description ([#PR](url))

Fixes
-----

*   [#ISSUE](url) Description ([#PR](url))

Other
-----

*   Description ([#PR](url))
```

Section order: UPGRADE TO NEW RELEASE → INFRASTRUCTURE CHANGELOG → BREAKING CHANGES (configuration) → BREAKING CHANGES (API) → Features → Fixes → Other.

Omit BREAKING CHANGES (configuration) and BREAKING CHANGES (API) entirely when they have no entries.

A delta `rc` release uses the same shape — no preamble paragraph, no header pointing at the previous rc.

## Return to the main conversation

Return a short summary — five lines or fewer. Include:

- The draft path (`claude/release-notes/<tag>-draft.md`).
- Counts of bullets per section after enhancement (including BREAKING CHANGES sections if present).
- Reclassifications that happened (e.g. "moved 2 from Other → Features, 1 from Other → BREAKING CHANGES (API)").
- Items dropped (count, with one example).
- Any open questions for the user (env-var name disagreement between PR body and source, ambiguous categorization, missing issue number).

Example:

> Drafted `claude/release-notes/0.17.0-rc.0-draft.md`. 1 BREAKING CHANGES (API), 6 Features, 8 Fixes, 12 Other. Reclassified 1 from Other → Features (`feat(area):`). Dropped 2 items (merge commits). One open: PR #870 body says `influx.timeout` but the settings class uses `influxdb.http.timeout` — used the code name; flagged in editorial notes.

## Safety rails

- **Never edit GitHub.** No `gh release edit`, no `gh release create`. Drafts only.
- **Never invent items.** Every kept bullet maps to a PR or a commit hash in the range.
- **Never silently rename or drop a PR reference.** Each bullet carries `[#ISSUE](url)` at the start and `([#PR](url))` at the end so links resolve on the release page.
- **Source code wins over PR descriptions.** If the PR body uses a different name than what's in the code, use the code name and note the discrepancy in the editorial-notes file.
- **Don't consolidate pre-release notes** into the stable's notes unless the user explicitly asks — each rc tag has its own page.
- **Match the terseness of the predecessor's notes.** If they're one-liners, your bullets are one-liners. Defer to the established style.

## Maintenance

Conventions drift as the project grows. If you notice a pattern in the raw CI notes that this skill doesn't handle (a new section the CI emits, a new conventional-commit scope that misroutes items, a recurring rewrite the user keeps asking for), surface it in your return summary and offer to update this `SKILL.md`. The user can confirm before any edit lands.