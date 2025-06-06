alter table if exists role_limit_entity drop column if exists (id);
alter table if exists role_limit_entity add primary key (deployment_id, role_id);
