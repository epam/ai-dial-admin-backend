alter table if exists interceptor_model add column if not exists interceptors_order integer;
alter table if exists interceptor_model_aud add column if not exists interceptors_order integer;

update interceptor_model set interceptors_order = 0;
update interceptor_model_aud set interceptors_order = 0;

alter table interceptor_model alter column interceptors_order set not null;
alter table interceptor_model_aud alter column interceptors_order set not null;

-- drop primary key in interceptor_model_aud table
do
$do$
declare pk_name text;
begin
    select tc.constraint_name into pk_name
    from information_schema.table_constraints tc
    where tc.table_name = 'interceptor_model_aud' and tc.constraint_type = 'PRIMARY KEY'
    limit 1;

    if pk_name is not null then
        execute format('alter table %s drop constraint %s', 'interceptor_model_aud', pk_name);
    end if;
end;
$do$;

alter table interceptor_model add primary key (model_name, interceptor_name, interceptors_order);
alter table interceptor_model_aud add primary key (model_name, rev, interceptor_name, interceptors_order);