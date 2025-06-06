INSERT INTO role_entity (id, description, name)
SELECT NEXT VALUE FOR role_entity_seq, NULL, 'default'
WHERE NOT EXISTS (
    SELECT 1 FROM role_entity WHERE name = 'default'
);