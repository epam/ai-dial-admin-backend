update key_entity set project = '_UNDEFINED_' where project is null or trim(project) = '';
update key_entity_aud set project = '_UNDEFINED_' where (project is null or trim(project) = '') and revtype != 2;
alter table key_entity alter column project nvarchar(max) not null;