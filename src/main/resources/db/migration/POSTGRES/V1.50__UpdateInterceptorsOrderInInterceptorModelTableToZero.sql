update interceptor_model set interceptors_order = 0;
update interceptor_model_aud set interceptors_order = 0;

alter table interceptor_model alter column interceptors_order set not null;
alter table interceptor_model_aud alter column interceptors_order set not null;

alter table interceptor_model_aud drop primary key;

alter table interceptor_model add primary key (model_name, interceptor_name, interceptors_order);
alter table interceptor_model_aud add primary key (model_name, rev, interceptor_name, interceptors_order);