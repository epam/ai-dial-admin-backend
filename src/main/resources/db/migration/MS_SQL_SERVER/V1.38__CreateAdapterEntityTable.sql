alter table model_entity add adapter_name nvarchar(255);

create table adapter_entity (
  name nvarchar(255) not null,
  display_name nvarchar(max),
  base_endpoint nvarchar(max),
  description nvarchar(max),
  primary key (name)
);

alter table model_entity add constraint FK_MODEL_ENTITY_ADAPTER_NAME foreign key (adapter_name) references adapter_entity (name);

create table adapter_entity_aud (
  rev integer not null,
  revtype tinyint,
  name nvarchar(255) not null,
  display_name nvarchar(max),
  base_endpoint nvarchar(max),
  description nvarchar(max),
  primary key (rev, name)
);

alter table adapter_entity_aud add constraint FK_REVINFO_ADAPTER_ENTITY_AUD foreign key (rev) references revinfo;

alter table model_entity_aud add adapter_name nvarchar(255);