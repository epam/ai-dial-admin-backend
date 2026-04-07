alter table audit_activity_entity add parent_activity_id varchar(36) null;
alter table audit_activity_entity add operation_metadata nvarchar(max) null;

alter table audit_activity_entity
    add constraint fk_audit_activity_parent foreign key (parent_activity_id) references audit_activity_entity (activity_id);

create index idx_audit_activity_parent on audit_activity_entity (parent_activity_id);
