-- add cost limit's fields to role_entity
alter table if exists role_entity add column if not exists cost_limit_minute numeric default 9223372036854775807 not null;
alter table if exists role_entity add column if not exists cost_limit_day numeric default 9223372036854775807 not null;
alter table if exists role_entity add column if not exists cost_limit_week numeric default 9223372036854775807 not null;
alter table if exists role_entity add column if not exists cost_limit_month numeric default 9223372036854775807 not null;

-- add cost limit's fields to role_entity_aud
alter table if exists role_entity_aud add column if not exists cost_limit_minute numeric;
alter table if exists role_entity_aud add column if not exists cost_limit_day numeric;
alter table if exists role_entity_aud add column if not exists cost_limit_week numeric;
alter table if exists role_entity_aud add column if not exists cost_limit_month numeric;

-- populate existing records with default value (except removals)
update role_entity_aud set cost_limit_minute = 9223372036854775807 where cost_limit_minute is null and revtype != 2;
update role_entity_aud set cost_limit_day = 9223372036854775807 where cost_limit_day is null and revtype != 2;
update role_entity_aud set cost_limit_week = 9223372036854775807 where cost_limit_week is null and revtype != 2;
update role_entity_aud set cost_limit_month = 9223372036854775807 where cost_limit_month is null and revtype != 2;
