-- set default value 0 for field version
update model_entity set version = 0 where version is null;

