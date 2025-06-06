create table assistants_property_entity (
  id bigint not null,

  endpoint varchar(255),

  rate_endpoint varchar(255),
  tokenize_endpoint varchar(255),
  truncate_prompt_endpoint varchar(255),
  configuration_endpoint varchar(255),

  system_prompt_supported boolean,
  tools_supported boolean,
  seed_supported boolean,
  url_attachments_supported boolean,
  folder_attachments_supported boolean,
  allow_resume boolean,
  accessible_by_per_request_key boolean,
  content_parts_supported boolean,
  temperature_supported boolean,
  addons_supported boolean,

  primary key (id)
);