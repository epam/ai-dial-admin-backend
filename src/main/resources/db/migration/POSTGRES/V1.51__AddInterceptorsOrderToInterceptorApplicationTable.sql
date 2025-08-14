alter table if exists interceptor_application add column if not exists interceptors_order integer;
alter table if exists interceptor_application_aud add column if not exists interceptors_order integer;

update interceptor_application set interceptors_order = 0;
update interceptor_application_aud set interceptors_order = 0;

alter table interceptor_application alter column interceptors_order set not null;
alter table interceptor_application_aud alter column interceptors_order set not null;

-- drop primary key in interceptor_application_aud table
do
$do$
declare pk_name text;
begin
    select tc.constraint_name into pk_name
    from information_schema.table_constraints tc
    where tc.table_name = 'interceptor_application_aud' and tc.constraint_type = 'PRIMARY KEY'
    limit 1;

    if pk_name is not null then
        execute format('alter table %s drop constraint %s', 'interceptor_application_aud', pk_name);
    end if;
end;
$do$;

alter table interceptor_application add primary key (application_name, interceptor_name, interceptors_order);
alter table interceptor_application_aud add primary key (application_name, rev, interceptor_name, interceptors_order);