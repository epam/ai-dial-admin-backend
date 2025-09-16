-- add cost limit's fields to role_entity
alter table role_entity add cost_limit_minute numeric;
alter table role_entity add constraint df_role_entity_cost_limit_minute default 9223372036854775807 for cost_limit_minute;
alter table role_entity alter column cost_limit_minute numeric not null;

alter table role_entity add cost_limit_day numeric;
alter table role_entity add constraint df_role_entity_cost_limit_day default 9223372036854775807 for cost_limit_day;
alter table role_entity alter column cost_limit_day numeric not null;

alter table role_entity add cost_limit_week numeric;
alter table role_entity add constraint df_role_entity_cost_limit_week default 9223372036854775807 for cost_limit_week;
alter table role_entity alter column cost_limit_week numeric not null;

alter table role_entity add cost_limit_month numeric;
alter table role_entity add constraint df_role_entity_cost_limit_month default 9223372036854775807 for cost_limit_month;
alter table role_entity alter column cost_limit_month numeric not null;

-- add cost limit's fields to role_entity_aud
alter table role_entity_aud add cost_limit_minute numeric;
alter table role_entity_aud add cost_limit_day numeric;
alter table role_entity_aud add cost_limit_week numeric;
alter table role_entity_aud add cost_limit_month numeric;

-- populate existing records with default value (except removals)
update role_entity_aud set cost_limit_minute = 9223372036854775807 where cost_limit_minute is null and revtype != 2;
update role_entity_aud set cost_limit_day = 9223372036854775807 where cost_limit_day is null and revtype != 2;
update role_entity_aud set cost_limit_week = 9223372036854775807 where cost_limit_week is null and revtype != 2;
update role_entity_aud set cost_limit_month = 9223372036854775807 where cost_limit_month is null and revtype != 2;
