-- Create global_interceptor_entity table
create table global_interceptor_entity (
  name varchar(255) not null,
  interceptor_order integer not null,
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  primary key (
      name, interceptor_order
    )
);

alter table global_interceptor_entity add constraint FK_GLOBAL_INTERCEPTOR_INTERCEPTOR_NAME foreign key (name) references interceptor_entity (name);

-- Create global_interceptor_entity_aud table
create table global_interceptor_entity_aud (
  rev integer not null,
  name varchar(255) not null,
  interceptor_order integer not null,
  created_at_ms bigint not null,
  updated_at_ms bigint not null,
  revtype smallint,
  primary key (
    name, rev, interceptor_order
  )
);