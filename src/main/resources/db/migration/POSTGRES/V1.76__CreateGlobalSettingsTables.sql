-- Create global_settings_entity table
create table global_settings_entity (
  id integer primary key,
  global_interceptors text,
  created_at_ms bigint not null,
  updated_at_ms bigint not null
);

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