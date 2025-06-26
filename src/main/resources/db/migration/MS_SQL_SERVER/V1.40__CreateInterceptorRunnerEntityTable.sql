create table interceptor_runner_entity (
  name nvarchar(255) not null,
  display_name nvarchar(max),
  description nvarchar(max),
  completion_endpoint nvarchar(max),
  configuration_endpoint nvarchar(max),
  primary key (name)
);

alter table interceptor_entity add configuration_endpoint nvarchar(max);
alter table interceptor_entity add interceptor_runner_name nvarchar(255);

alter table interceptor_entity add constraint FK_INTERCEPTOR_ENTITY_INTERCEPTOR_RUNNER_NAME
    foreign key (interceptor_runner_name) references interceptor_runner_entity (name);