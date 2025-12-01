-- Create global_settings_entity table
create table global_settings_entity (
  id integer primary key,
  global_interceptors text,
  created_at_ms bigint not null,
  updated_at_ms bigint not null
);

insert into global_settings_entity  (id, global_interceptors, created_at_ms, updated_at_ms)
VALUES (1, '[]', 
DATEDIFF_BIG(millisecond, '1970-01-01 00:00:00', SYSUTCDATETIME()), 
DATEDIFF_BIG(millisecond, '1970-01-01 00:00:00', SYSUTCDATETIME()));

-- Create global_settings_entity_aud table
create table global_settings_entity_aud (
  rev integer not null,
  id integer not null,
  global_interceptors text,
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  revtype smallint,
  primary key (
    id, rev
  )
);