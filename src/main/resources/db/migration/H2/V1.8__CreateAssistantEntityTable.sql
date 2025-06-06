create table assistant_entity (
  id bigint not null,
  defaults clob,
  description varchar(2048),
  description_keywords varchar(255) array,
  display_name varchar(255),
  forward_auth_token boolean,
  icon_url varchar(255),
  input_attachment_types varchar(255) array,
  max_input_attachments integer,
  deployment_id bigint,
  primary key (id)
);
alter table assistant_entity add constraint UK_ASSISTANT_ENTITY_DEPLOYMENT_ID unique (deployment_id);
alter table assistant_entity add constraint FK_ASSISTANT_ENTITY_DEPLOYMENT_ID foreign key (deployment_id) references deployment_entity (id);
create sequence if not exists assistant_entity_seq start with 1 increment by 50;