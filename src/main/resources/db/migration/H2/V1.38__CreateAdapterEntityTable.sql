alter table if exists model_entity add column adapter_name varchar(255);

create table adapter_entity (
  name varchar(255) not null,
  display_name TEXT,
  base_endpoint TEXT,
  description TEXT,
  primary key (name)
);

alter table model_entity add constraint FK_MODEL_ENTITY_ADAPTER_NAME foreign key (adapter_name) references adapter_entity (name);

create table adapter_entity_aud (
  rev integer not null,
  revtype tinyint,
  name varchar(255) not null,
  display_name TEXT,
  base_endpoint TEXT,
  description TEXT,
  primary key (rev, name)
);

alter table if exists adapter_entity_aud add constraint FK_REVINFO_ADAPTER_ENTITY_AUD foreign key (rev) references revinfo;

alter table if exists model_entity_aud add column adapter_name varchar(255);