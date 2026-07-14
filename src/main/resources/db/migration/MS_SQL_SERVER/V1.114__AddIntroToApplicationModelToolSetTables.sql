-- add intro field to application tables
if not exists (select 1 from sys.columns where object_id = object_id('application_entity') and name = 'intro')
    alter table application_entity add intro varchar(255);

if not exists (select 1 from sys.columns where object_id = object_id('application_entity_aud') and name = 'intro')
    alter table application_entity_aud add intro varchar(255);

-- add intro field to model tables
if not exists (select 1 from sys.columns where object_id = object_id('model_entity') and name = 'intro')
    alter table model_entity add intro varchar(255);

if not exists (select 1 from sys.columns where object_id = object_id('model_entity_aud') and name = 'intro')
    alter table model_entity_aud add intro varchar(255);

-- add intro field to tool_set tables
if not exists (select 1 from sys.columns where object_id = object_id('tool_set_entity') and name = 'intro')
    alter table tool_set_entity add intro varchar(255);

if not exists (select 1 from sys.columns where object_id = object_id('tool_set_entity_aud') and name = 'intro')
    alter table tool_set_entity_aud add intro varchar(255);
