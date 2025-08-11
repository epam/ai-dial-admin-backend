alter table interceptor_application add interceptors_order integer;
alter table interceptor_application_aud add interceptors_order integer;
go

update interceptor_application set interceptors_order = 0;
update interceptor_application_aud set interceptors_order = 0;

alter table interceptor_application alter column interceptors_order integer not null;
alter table interceptor_application_aud alter column interceptors_order integer not null;
go

-- drop primary key in interceptor_application_aud table
declare @pkName nvarchar(128);
declare @sql nvarchar(max);

select @pkName = kc.name
from sys.key_constraints kc
join sys.tables t on kc.parent_object_id = t.object_id
where t.name = 'interceptor_application_aud' and kc.type = 'PK';

if @pkName is not null
begin
    set @sql = 'alter table interceptor_application_aud drop constraint [' + @pkName + ']';
    exec sp_executesql @sql;
end

alter table interceptor_application add constraint PK_INTERCEPTOR_APPLICATION primary key nonclustered (application_name, interceptor_name, interceptors_order);
alter table interceptor_application_aud add constraint PK_INTERCEPTOR_APPLICATION_AUD primary key nonclustered (application_name, rev, interceptor_name, interceptors_order);