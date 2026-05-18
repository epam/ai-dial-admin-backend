# Feasibility Analysis: Hierarchical Price-Breakdown Dashboard — Backend Scope

## Context

A design proposal calls for a dashboard where the spend table is a *tree*: a model's total cost (e.g., `Mistral-7B-Instruct $15`) expands into its constituent costs (e.g., `Model Flex $5`, `RAG Flex $5`, `Application DALL $5`), and each child can itself be expanded recursively. An accompanying implementation hint in the original discussion suggested "use AI, like the Evaluation scenario does."

This document evaluates feasibility **strictly from the backend side** (this repo, `ai-dial-admin-backend`, the `metric/` module). Frontend rendering, AG Grid wiring, etc. are out of scope except where they shape the API contract.

---

## Verdict

- **Feature:** FEASIBLE — entirely against the existing telemetry, using the existing `JsonDataQuery` DSL (`groupBy`, `sum`, `count`) without any new operator.
- **Implementation:** one deterministic query plus a small catalog endpoint. The tree itself is assembled client-side from the call-chain strings the query returns. No AI, no per-row enrichment, no span-id or trace-id correlation.

---

## Flows (TL;DR)

| # | Flow | Where | What |
|---|---|---|---|
| 1 | **Load** | BE | One `JsonDataQuery`: `GROUP BY execution_path, deployment` over `[from, to]`. Returns `{executionPath: List<String>, deployment, totalCost, ownCost, invocations}` per distinct call chain. |
| 2 | **Render tree** | UI | Feed each `executionPath` array into AG Grid Tree Data's `getDataPath`. Shared prefixes merge automatically; each row is one node. |
| 3 | **Filter (search `X`)** | UI | `rows.filter(r => r.deployment.includes(X))`, then union with each match's ancestor paths already in the payload. Correct totals at every visible level. |
| 4 | **`hasChildren`** | UI | A node has a chevron iff some other returned `executionPath` strictly extends it. |
| 5 | **Type column** | UI | Join `deployment` against `GET /api/v1/deployments/type-map` (cached per session). |

Everything below is the detailed version of the same thing.

---

## What is already available from the existing data model

Per the [`ai-dial-analytics-realtime` README](https://github.com/epam/ai-dial-analytics-realtime/blob/development/README.md), every chat/embedding/MCP record in InfluxDB carries the fields the dashboard needs:

| Field | Role |
|---|---|
| `execution_path` (tag) | Full call chain to this record, e.g. `Mistral,ModelFlex,ModelLLM`. This is the tree skeleton. |
| `deployment_price` (double) | "Own cost" of this call — excludes anything it triggered. |
| `price` (double) | "Rolled-up cost" — this call plus everything it directly/indirectly triggered. |
| `_time` (time) | Bucket selector. |
| `user_hash`, `chat_id` (tags) | Optional scoping. |

The metric module already declares these as queryable columns in `src/main/resources/metric.config.influx2.json` and `metric.config.influx3.json` (lines 48–62, 144–148), so the dual-engine query path (`MetricService` → `AbstractQueryBuilder` → `FluxQueryBuilder` / `SqlQueryBuilder`) handles them today without schema changes.

---

## Design — group by `execution_path`, let the UI assemble the tree

The full hierarchy is already encoded in the `execution_path` tag. Grouping by it over the period yields exactly one row per distinct call chain, with that chain's aggregated cost across every invocation. Each row corresponds to one tree node; the tree's edges are implicit in the path strings (`A,B` is a child of `A`). The UI splits each path on the delimiter and feeds the resulting array straight into a tree component — AG Grid Tree Data's `getDataPath` callback accepts exactly this shape. Shared prefixes merge automatically.

### Flow — one query per dashboard load

```json
{
  "$type": "json",
  "fillGaps": false,
  "query": {
    "from": "analytics",
    "expressions": [
      "execution_path",
      "deployment",
      "sum(price) as totalCost",
      "sum(deployment_price) as ownCost",
      "count() as invocations"
    ],
    "where": {
      "$and": [
        { "$gte": { "left": "_time", "right": "':from'" } },
        { "$lt":  { "left": "_time", "right": "':to'"   } }
      ]
    },
    "groupBy": ["execution_path", "deployment"]
  }
}
```

`deployment` is the last element of `execution_path`, so it's functionally determined by it — adding it to `groupBy` doesn't change row cardinality, it just promotes the value into the projection so the UI can label rows and join the type-map without having to know the path delimiter.

Optional scoping — append to the `$and` block: `{"$eq": {"left": "user_hash", "right": "':userId'"}}` and/or `{"$eq": {"left": "chat_id", "right": "':chatId'"}}`.

Entity-name search (the user types `X` in a search box) is handled UI-side — see "Filtering by entity X" below. The backend query does not change.

**Output (after backend splits `execution_path` on the verified delimiter):** flat list of `{ executionPath: List<String>, deployment, totalCost, ownCost, invocations }`. For the example tree:

| executionPath | deployment | totalCost | ownCost |
|---|---|---|---|
| `["Mistral"]` | Mistral | 15 | 5 |
| `["Mistral","ModelFlex"]` | ModelFlex | 5 | 5 |
| `["Mistral","RAG"]` | RAG | 5 | 5 |
| `["Mistral","AppDall"]` | AppDall | 5 | 1 |
| `["Mistral","AppDall","ModelDall"]` | ModelDall | 4 | 4 |
| `["Mistral","AppDall","WebSearch"]` | WebSearch | 1 | 0 |
| `["AppFoo"]` | AppFoo | … | … |
| `["AppFoo","Mistral"]` | Mistral | … | … |

The UI uses `executionPath` as AG Grid's `getDataPath` value, `deployment` as the display label and the join key against `/api/v1/deployments/type-map`. No second query, no lazy expansion, no chevron-state RPC.

### What falls out for free

- **Top-level vs nested separation.** `["Mistral"]` and `["AppFoo","Mistral"]` are different rows with different aggregated costs. The same deployment appearing at the top *and* inside another tree is handled by the data shape, not by extra predicates.
- **`hasChildren`.** The UI has the full path set; a node has a chevron iff some other returned path strictly extends it. Zero backend involvement.
- **Type column.** Separate `GET /api/v1/deployments/type-map` lookup, cached client-side and joined by deployment name. Avoids per-row server-side resolution and stays in sync with the catalog automatically.
- **Cost reconciliation per node.** Each row's `totalCost` is its rolled-up subtree cost; `ownCost` is its intrinsic cost. The UI shows both and the difference at each node is "cost contributed by descendants" — matches the visual mental model exactly.

### Filtering by entity X

Filtering happens **UI-side**, against the full payload the single backend query already returned. The backend filter is not used for entity-name search.

Why not server-side:

- The search box supports **partial matches**, so a server filter would have to be substring-based. `$contains` on `execution_path` runs into delimiter collisions (e.g. `"Web"` matching both `"Web Search"` and `"Webhook"`), and a delimiter-aware `$like` only works for exact catalog names — not for the partial-match UX.
- `$contains` on `deployment` alone is well-defined but only marks *which leaf rows* match X. To render a usable tree the UI also needs every matched row's **ancestor rows** (to display parent totals correctly), and those ancestors don't satisfy the filter on their own. Backfilling them would require an additional query (e.g. a second pass keyed by the matched paths' prefixes), which defeats the simplicity of the one-query design.
- Whereas the full payload is already in the browser. A partial-match filter against the in-memory list of rows is a few lines of JS, exact in semantics, instant in latency.

UI behaviour:

1. The UI holds the full list of `{ executionPath, deployment, totalCost, ownCost, invocations }` rows for the period.
2. As the user types `X`, the UI computes `matchedDeployments = rows where deployment.toLowerCase().includes(X.toLowerCase())`.
3. The visible set is the union of all matched rows **plus all of their ancestor rows** (every prefix of a matched row's `executionPath` that's present in the payload). This keeps the tree renderable and the displayed totals correct — each visible node is still backed by its real aggregated row, so `totalCost` and `ownCost` are accurate at every level.
4. The tree is re-rendered from that filtered set; AG Grid's `getDataPath` and the prefix-merging behaviour do the rest.

Payload-size assumption: this design assumes the whole period's distinct-path set fits in one response. That's covered by the `execution_path` cardinality risk below — if it ever breaks, the fallback is to paginate the backend response by root deployment (first element of the path), not to push filtering into the backend.

---

## Proposed backend surface

Two endpoints, both gated by the existing `metrics.enabled` feature flag and (new) per-user authorization.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/metrics/cost-trees` | Returns the flat list of aggregated `{executionPath, deployment, totalCost, ownCost, invocations}` rows for a time range, optionally scoped by `userId` / `chatId` / `containsDeployment`. Builds the `JsonDataQuery` shown above, calls `MetricService.getData()`, splits the `execution_path` strings on the verified delimiter before serializing. |
| `GET`  | `/api/v1/deployments/type-map` | Returns `Map<deploymentName, DeploymentType>` derived from the in-memory deployment catalog; cached, invalidated on config reload. |

---

## What needs to change in the metric module

1. **No DSL operator additions required.** `groupBy`, `sum`, `count`, `$gte`/`$lt`, `$eq`, `$contains`, `$like` are all already supported.
2. **New endpoints + DTOs:** `CostTreeRequest` / `CostTreeRowDto` (with `executionPath: List<String>`, `deployment: String`, `totalCost`, `ownCost`, `invocations`) and `DeploymentTypeMapDto`. The controller builds the `JsonDataQuery`, delegates to `MetricService.getData()`, and post-processes by splitting the `execution_path` strings — the UI never sees the raw tag encoding.
3. **Catalog-type endpoint:** a small service iterating `CoreApplication`, `CoreModel`, `CoreAddon`, `CoreInterceptor`, `CoreAssistant` from in-memory config; cached, invalidated on config reload.
4. **RBAC:** today `MetricController` is gated only by `IsMetricsEnabledCondition` (`src/main/java/com/epam/aidial/cfg/features/IsMetricsEnabledCondition.java`); there is no per-user authorization. A cost dashboard almost certainly needs admin-only or per-tenant scoping — confirm intent and add a role check before exposing.
5. **Tests:** dual-engine integration tests (Flux + SQL) asserting:
   - Grouping on the `execution_path` tag returns identical results from both engines.
   - For a known fixture with no intrinsic-cost parents, the root row's `totalCost` equals the sum of its descendants' `ownCost` plus the root's own `ownCost` within rounding.
   - Optional `containsDeployment` filter returns exactly the paths containing the value, with the delimiter-aware pattern preventing substring collisions.

---

## Risks and clarifications

1. **`execution_path` tag cardinality (most important risk).** Tags are the basis of InfluxDB's inverted index; very high-cardinality tags (millions of distinct values) degrade write/query performance and bloat the index. Confirm `ai-dial-analytics-realtime` keeps `execution_path` bounded (deployment names only, no per-conversation or per-user data embedded) and isn't already under memory pressure on this column.
2. **Payload size scales with distinct-path count, not with traffic.** A period with 1 M requests but ~500 distinct call chains is ~500 rows. A long period across a large multi-tenant deployment could grow into the tens of thousands. Mitigation: enforce `maxTimeRange` strictly on the cost-tree endpoint; if it bites at scale, paginate by first-element-of-path (i.e., by top-level root deployment).
3. **`execution_path` encoding contract.** The exact delimiter, surrounding characters, and escaping rules of the tag value must be known to the backend (for splitting) and to the delimiter-aware `$like` pattern (for server-side filtering, if adopted). Verification step listed below.
4. **Parents with intrinsic cost.** If a parent's `price` strictly exceeds the sum of its descendants' `price` (an app charging a flat fee in addition to invoking models), the UI's "cost from descendants" computation will not balance exactly. The response already exposes both `totalCost` and `ownCost`, so the UI can display both transparently — document the semantic.
5. **Time-range alignment with `CostLimitEntity`.** The backend already has per-role cost limits (`src/main/java/com/epam/aidial/cfg/dao/model/CostLimitEntity.java`). If this dashboard's totals are meant to reconcile with limit enforcement, both must read from the same source-of-truth and the same time-window definition (calendar day in user TZ vs. rolling 24 h vs. UTC day) — align explicitly.

---

## Critical files for reference

- `src/main/java/com/epam/aidial/metric/web/controller/MetricController.java` — controller to extend (or a sibling `CostTreeController`).
- `src/main/java/com/epam/aidial/metric/service/MetricService.java` — orchestration; reuse `getData(...)`.
- `src/main/resources/metric.config.influx2.json`, `metric.config.influx3.json` — confirm `execution_path`, `deployment_price`, `price` are declared in the dataset used by this dashboard (cost-tree fields are at lines 48–62 / 144–148).
- `src/main/java/com/epam/aidial/metric/CLAUDE.md` — dual-engine and column-aliasing constraints to honour.
- `src/main/java/com/epam/aidial/cfg/features/IsMetricsEnabledCondition.java` — the feature flag; add the RBAC layer alongside.
- `src/main/java/com/epam/aidial/core/config/CoreModel.java`, `CoreApplication.java`, `CoreAddon.java`, `CoreInterceptor.java`, `CoreAssistant.java` — sources for the type-map endpoint.

---

## Verification (when implemented)

1. **Schema audit:** confirm `execution_path`, `deployment_price`, `price` are populated for every record type the dashboard covers (chat completion, embedding, MCP toolset, route).
2. **Encoding audit:** document the exact `execution_path` serialization (delimiter, escaping). The backend split logic and the optional `$contains` filter both depend on it.
3. **Cardinality audit:** estimate distinct `execution_path` values per day in production. Validate this stays in the thousands, not millions.
4. **Dual-engine parity:** identical request against InfluxDB 2 and InfluxDB 3 datasets returns equal results.
5. **Cost reconciliation:** for a fixture with no intrinsic-cost parents, root `totalCost` equals sum of descendants' `ownCost` + root's own `ownCost`, within rounding.
6. **Type map:** `GET /api/v1/deployments/type-map` returns every `deployment` value present in any non-empty cost-tree row.
7. **Authorization:** non-admin users cannot reach `/api/v1/metrics/cost-trees` or `/api/v1/deployments/type-map`.

---

## Recommended next steps

1. Verify `execution_path` is populated and bounded (no high-cardinality embeds) and document its serialization.
2. Confirm with the frontend team that the full-period response is acceptable to hold in memory and filter against client-side. The cardinality concern (distinct `execution_path` count) is what determines this; document the expected order of magnitude.
3. Decide RBAC scope for the new endpoints (admin-only? per-tenant?) and add it as part of the same change.
