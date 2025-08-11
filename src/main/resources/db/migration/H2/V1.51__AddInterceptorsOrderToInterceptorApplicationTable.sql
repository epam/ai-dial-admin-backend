alter table if exists interceptor_application add column if not exists interceptors_order integer;
alter table if exists interceptor_application_aud add column if not exists interceptors_order integer;

update interceptor_application set interceptors_order = 0;
update interceptor_application_aud set interceptors_order = 0;

alter table interceptor_application alter column interceptors_order set not null;
alter table interceptor_application_aud alter column interceptors_order set not null;

alter table interceptor_application_aud drop primary key;

alter table interceptor_application add primary key (application_name, interceptor_name, interceptors_order);
alter table interceptor_application_aud add primary key (application_name, rev, interceptor_name, interceptors_order);