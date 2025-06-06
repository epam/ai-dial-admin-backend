set @currentTimestamp = datediff('ms', timestamp '1970-01-01 00:00:00.000', current_timestamp at time zone 'UTC');
alter table key_entity add project_contact_point text;
alter table key_entity add created_at_ms bigint not null default @currentTimestamp;
alter table key_entity add expires_at_ms bigint;
alter table key_entity add key_value_generated_at_ms bigint not null default @currentTimestamp;