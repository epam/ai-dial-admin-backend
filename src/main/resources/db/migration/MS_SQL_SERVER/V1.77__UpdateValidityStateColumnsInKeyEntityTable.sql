update key_entity
set
  validity_state_message = case
    when not exists (
      select 1 from role_key where role_key.key_name = key_entity.name
    ) and key_value is not null and trim(key_value) <> ''
      then 'No roles assigned'
    when exists (
      select 1 from role_key where role_key.key_name = key_entity.name
    ) and (key_value is null or trim(key_value) = '')
      then 'Key value is missing'
    when not exists (
      select 1 from role_key where role_key.key_name = key_entity.name
    ) and (key_value is null or trim(key_value) = '')
      then 'No roles assigned, Key value is missing'
    else null
  end,
  validity_state_is_valid = case
    when exists (
      select 1 from role_key where role_key.key_name = key_entity.name
    ) and key_value is not null and trim(key_value) <> ''
      then 1
    else 0
  end;