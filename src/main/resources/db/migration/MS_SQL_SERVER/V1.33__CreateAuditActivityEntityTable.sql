create table audit_activity_entity (
  activity_id varchar(36) not null,
  activity_type varchar(36) not null,
  resource_type varchar(36),
  resource_id nvarchar(255),
  epoch_timestamp_ms bigint,
  initiated_author nvarchar(255),
  initiated_email nvarchar(320),
  revision integer,
  primary key (activity_id)
);

alter table audit_activity_entity add constraint FK_REVINFO_AUDIT_ACTIVITY_ENTITY foreign key (revision) references revinfo;
