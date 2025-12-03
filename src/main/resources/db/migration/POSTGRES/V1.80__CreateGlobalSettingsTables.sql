-- NOTE ABOUT INITIAL GLOBAL SETTINGS AUDIT ENTRY
-- We intentionally do NOT write a "create" audit record for the initial global settings row during this migration.

-- Clarification:
-- - The global settings row itself IS created by this migration script
-- - We deliberately do NOT insert a corresponding "create" entry into the audit tables.
-- - Only subsequent changes to global settings will be recorded in the audit.

-- Create global_settings_entity table
create table global_settings_entity (
  id integer primary key,
  global_interceptors varchar(255),
  created_at_ms bigint not null,
  updated_at_ms bigint not null
);

insert into global_settings_entity  (id, global_interceptors, created_at_ms, updated_at_ms) VALUES (1, '[]', 
EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000,
EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000);

-- Create global_settings_entity_aud table
create table global_settings_entity_aud (
  rev integer not null,
  id integer not null,
  global_interceptors varchar(255),
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  revtype smallint,
  primary key (
    id, rev
  )
);