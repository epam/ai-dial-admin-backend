INSERT INTO role_entity (name, description) VALUES ('default', NULL);

INSERT INTO revinfo (author, timestamp, id)
VALUES ('system', EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000, default);

INSERT INTO role_entity_aud (rev, revtype, name, description)
SELECT 1, 0, name, description FROM role_entity;