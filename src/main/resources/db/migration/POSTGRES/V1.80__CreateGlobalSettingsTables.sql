-- Create global_settings_entity table
create table global_settings_entity (
  id integer primary key,
  global_interceptors varchar(2048),
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
  global_interceptors varchar(2048),
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  revtype smallint,
  primary key (
    id, rev
  )
);

insert into global_settings_entity_aud (rev, id, global_interceptors, created_at_ms, updated_at_ms, revtype)
select
  r.id,
  g.id,
  g.global_interceptors,
  r.timestamp,
  r.timestamp,
  0
from global_settings_entity g
cross join revinfo r
where r.id = 1;

insert into audit_activity_entity (activity_id, activity_type, resource_type, resource_id, initiated_author, initiated_email, revision, epoch_timestamp_ms)
values (
   gen_random_uuid(),
  'Create',
  'GlobalSettings',
  1,
  'system',
  null,
  1,
  (select timestamp from revinfo where id = 1)
);