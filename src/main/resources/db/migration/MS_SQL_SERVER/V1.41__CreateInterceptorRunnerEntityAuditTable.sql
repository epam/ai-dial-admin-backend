alter table interceptor_entity_aud add configuration_endpoint text;
alter table interceptor_entity_aud add interceptor_runner_name nvarchar(255);

create table interceptor_runner_entity_aud (
  name nvarchar(255) not null,
  rev integer not null,
  revtype tinyint,
  display_name nvarchar(max),
  description nvarchar(max),
  completion_endpoint nvarchar(max),
  configuration_endpoint nvarchar(max),
  primary key (rev, name)
);

alter table interceptor_runner_entity_aud add constraint FK_REVINFO_INTERCEPTOR_RUNNER_ENTITY_AUD foreign key (rev) references revinfo;