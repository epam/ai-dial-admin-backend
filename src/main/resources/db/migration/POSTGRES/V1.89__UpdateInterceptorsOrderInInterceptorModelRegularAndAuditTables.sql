-- REGULAR TABLE

-- Find all groups (with more than 1 rows) which have interceptors_order = 0 for all rows in group
-- and store them in temporary all_zero_groups table
CREATE TEMP TABLE all_zero_groups AS
SELECT model_name
FROM interceptor_model
GROUP BY model_name
HAVING SUM(CASE WHEN interceptors_order <> 0 THEN 1 ELSE 0 END) = 0 AND COUNT(*) > 1;

-- For all groups in all_zero_groups table calculate correct interceptors_order value
-- and store result in temporary interceptor_model_ranked table
CREATE TEMP TABLE interceptor_model_ranked AS
SELECT
    a.model_name,
    a.interceptor_name,
    ROW_NUMBER() OVER (
        PARTITION BY a.model_name
        ORDER BY a.interceptor_name
    ) - 1 AS new_order
FROM interceptor_model a
JOIN all_zero_groups g
  ON a.model_name = g.model_name;

-- Update interceptors_order in interceptor_model according to values from temporary interceptor_model_ranked table
UPDATE interceptor_model t
SET interceptors_order = (
    SELECT r.new_order
    FROM interceptor_model_ranked r
    WHERE r.model_name = t.model_name
      AND r.interceptor_name = t.interceptor_name
);

-- Drop temporary tables
DROP TABLE interceptor_model_ranked;
DROP TABLE all_zero_groups;

-- AUDIT TABLE

-- Find all groups (with more than 1 rows) which have interceptors_order = 0 for all rows in group
-- and store them in temporary all_zero_groups table
CREATE TEMP TABLE all_zero_groups AS
SELECT model_name, rev
FROM interceptor_model_aud
GROUP BY model_name, rev
HAVING SUM(CASE WHEN interceptors_order <> 0 THEN 1 ELSE 0 END) = 0 AND COUNT(*) > 1;

-- For all groups in all_zero_groups table calculate correct interceptors_order value
-- and store result in temporary interceptor_model_aud_ranked table
CREATE TEMP TABLE interceptor_model_aud_ranked AS
SELECT
    a.model_name,
    a.rev,
    a.interceptor_name,
    ROW_NUMBER() OVER (
        PARTITION BY a.model_name, a.rev
        ORDER BY a.interceptor_name
    ) - 1 AS new_order
FROM interceptor_model_aud a
JOIN all_zero_groups g
  ON a.model_name = g.model_name
 AND a.rev = g.rev;

-- Update interceptors_order in interceptor_model_aud according to values from temporary interceptor_model_aud_ranked table
UPDATE interceptor_model_aud t
SET interceptors_order = (
    SELECT r.new_order
    FROM interceptor_model_aud_ranked r
    WHERE r.model_name = t.model_name
      AND r.rev = t.rev
      AND r.interceptor_name = t.interceptor_name
);

-- Drop temporary tables
DROP TABLE interceptor_model_aud_ranked;
DROP TABLE all_zero_groups;