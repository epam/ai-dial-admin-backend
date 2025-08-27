-- set default value 0 for field version to adapter_entity table
update adapter_entity set version = 0 where version is null;

-- set default value 0 for field version to model_entity table
update model_entity set version = 0 where version is null;

-- set default value 0 for field version to role_entity table
update role_entity set version = 0 where version is null;

-- set default value 0 for field version to route_entity table
update route_entity set version = 0 where version is null;

-- set default value 0 for field version to addon_entity table
update addon_entity set version = 0 where version is null;

-- set default value 0 for field version to application_entity table
update application_entity set version = 0 where version is null;

-- set default value 0 for field version to key_entity table
update key_entity set version = 0 where version is null;

