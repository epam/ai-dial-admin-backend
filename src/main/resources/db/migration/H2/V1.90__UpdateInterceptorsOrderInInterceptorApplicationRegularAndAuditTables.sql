-- REGULAR TABLE

-- Find all groups (with more than 1 rows) which have interceptors_order = 0 for all rows in group
-- and store them in temporary all_zero_groups table
CREATE TEMP TABLE all_zero_groups AS
SELECT application_name
FROM interceptor_application
GROUP BY application_name
HAVING SUM(CASE WHEN interceptors_order <> 0 THEN 1 ELSE 0 END) = 0 AND COUNT(*) > 1;

-- For all groups in all_zero_groups table calculate correct interceptors_order value
-- and store result in temporary interceptor_application_ranked table
CREATE TEMP TABLE interceptor_application_ranked AS
SELECT
    a.application_name,
    a.interceptor_name,
    ROW_NUMBER() OVER (
        PARTITION BY a.application_name
        ORDER BY a.interceptor_name
    ) - 1 AS new_order
FROM interceptor_application a
JOIN all_zero_groups g
  ON a.application_name = g.application_name;

-- Update interceptors_order in interceptor_application according to values from temporary interceptor_application_ranked table
UPDATE interceptor_application t
SET interceptors_order = (
    SELECT r.new_order
    FROM interceptor_application_ranked r
    WHERE r.application_name = t.application_name
      AND r.interceptor_name = t.interceptor_name
)
WHERE EXISTS (
    SELECT 1
    FROM interceptor_application_ranked r
    WHERE r.application_name = t.application_name
      AND r.interceptor_name = t.interceptor_name
);  

-- Drop temporary tables
DROP TABLE interceptor_application_ranked;
DROP TABLE all_zero_groups;

-- AUDIT TABLE

-- Find all groups (with more than 1 rows) which have interceptors_order = 0 for all rows in group
-- and store them in temporary all_zero_groups table
CREATE TEMP TABLE all_zero_groups AS
SELECT application_name, rev
FROM interceptor_application_aud
GROUP BY application_name, rev
HAVING SUM(CASE WHEN interceptors_order <> 0 THEN 1 ELSE 0 END) = 0 AND COUNT(*) > 1;

-- For all groups in all_zero_groups table calculate correct interceptors_order value
-- and store result in temporary interceptor_application_aud_ranked table
CREATE TEMP TABLE interceptor_application_aud_ranked AS
SELECT
    a.application_name,
    a.rev,
    a.interceptor_name,
    ROW_NUMBER() OVER (
        PARTITION BY a.application_name, a.rev
        ORDER BY a.interceptor_name
    ) - 1 AS new_order
FROM interceptor_application_aud a
JOIN all_zero_groups g
  ON a.application_name = g.application_name
 AND a.rev = g.rev;

-- Update interceptors_order in interceptor_application_aud according to values from temporary interceptor_application_aud_ranked table
UPDATE interceptor_application_aud t
SET interceptors_order = (
    SELECT r.new_order
    FROM interceptor_application_aud_ranked r
    WHERE r.application_name = t.application_name
      AND r.rev = t.rev
      AND r.interceptor_name = t.interceptor_name
)
WHERE EXISTS (
    SELECT 1
    FROM interceptor_application_aud_ranked r
    WHERE r.application_name = t.application_name
      AND r.rev = t.rev
      AND r.interceptor_name = t.interceptor_name
);

-- Drop temporary tables
DROP TABLE interceptor_application_aud_ranked;
DROP TABLE all_zero_groups;