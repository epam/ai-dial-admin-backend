INSERT INTO role_entity (name, description) VALUES ('default', NULL);

INSERT INTO revinfo (author, timestamp)
VALUES ('system', DATEDIFF_BIG(millisecond, '1970-01-01 00:00:00', SYSUTCDATETIME()));

INSERT INTO role_entity_aud (rev, revtype, name, description)
SELECT 1, 0, name, description FROM role_entity;