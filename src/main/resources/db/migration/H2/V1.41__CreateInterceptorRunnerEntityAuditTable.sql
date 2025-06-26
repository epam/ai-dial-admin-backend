alter table interceptor_entity_aud add configuration_endpoint text;
alter table interceptor_entity_aud add interceptor_runner_name varchar(255);

create table interceptor_runner_entity_aud (
  name varchar(255) not null,
  rev integer not null,
  revtype tinyint,
  display_name text,
  description text,
  completion_endpoint text,
  configuration_endpoint text,
  primary key (rev, name)
);

alter table if exists interceptor_runner_entity_aud add constraint FK_REVINFO_INTERCEPTOR_RUNNER_ENTITY_AUD foreign key (rev) references revinfo;