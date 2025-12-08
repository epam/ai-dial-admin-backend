create table admin_settings_entity (
  id integer primary key,
  core_config_version varchar(32),
  created_at_ms bigint not null,
  updated_at_ms bigint not null
);

insert into admin_settings_entity (id, core_config_version, created_at_ms, updated_at_ms)
values (1, null, (select timestamp from revinfo where id = 1), (select timestamp from revinfo where id = 1));

create table admin_settings_entity_aud (
  rev integer not null,
  id integer not null,
  core_config_version varchar(32),
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  revtype smallint,
  primary key (rev, id)
);

insert into admin_settings_entity_aud (rev, id, core_config_version, created_at_ms, updated_at_ms, revtype)
select 1, ase.id, ase.core_config_version, ase.created_at_ms, ase.updated_at_ms, 0
from admin_settings_entity ase
where ase.id = 1;

insert into audit_activity_entity (activity_id, activity_type, resource_type, resource_id, initiated_author, initiated_email, revision, epoch_timestamp_ms)
values (random_uuid(), 'Create', 'AdminSettings', 1, 'system', null, 1, (select timestamp from revinfo where id = 1));