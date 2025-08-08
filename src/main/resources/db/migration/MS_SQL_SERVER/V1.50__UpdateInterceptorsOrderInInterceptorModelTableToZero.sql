update interceptor_model set interceptors_order = 0;
update interceptor_model_aud set interceptors_order = 0;

alter table interceptor_model alter column interceptors_order integer not null;
alter table interceptor_model_aud alter column interceptors_order integer not null;
go

-- drop primary key in interceptor_model_aud table
declare @pkName nvarchar(128);
declare @sql nvarchar(max);

select @pkName = kc.name
from sys.key_constraints kc
join sys.tables t on kc.parent_object_id = t.object_id
where t.name = 'interceptor_model_aud' and kc.type = 'PK';

if @pkName is not null
begin
    set @sql = 'alter table interceptor_model_aud drop constraint [' + @pkName + ']';
    exec sp_executesql @sql;
end

alter table interceptor_model add constraint PK_INTERCEPTOR_MODEL primary key nonclustered (model_name, interceptor_name, interceptors_order);
alter table interceptor_model_aud add constraint PK_INTERCEPTOR_MODEL_AUD primary key nonclustered (model_name, rev, interceptor_name, interceptors_order);