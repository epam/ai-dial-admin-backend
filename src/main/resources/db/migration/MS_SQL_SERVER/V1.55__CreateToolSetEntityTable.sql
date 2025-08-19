-- Create tool_set_entity table
CREATE TABLE tool_set_entity (
  deployment_name NVARCHAR(255) NOT NULL,
  display_name NVARCHAR(255),
  description NVARCHAR(max),
  description_keywords VARBINARY(max),
  endpoint NVARCHAR(max),
  icon_url NVARCHAR(max),
  max_retry_attempts INTEGER,
  author NVARCHAR(max),
  created_at_ms BIGINT NOT NULL,
  updated_at_ms BIGINT NOT NULL,
  transport NVARCHAR(max),
  allowed_tools VARBINARY(max),
  PRIMARY KEY (deployment_name)
);

ALTER TABLE tool_set_entity ADD CONSTRAINT FK_TOOL_SET_ENTITY_DEPLOYMENT_NAME FOREIGN KEY (deployment_name) REFERENCES deployment_entity (name);

-- Create tool_set_entity_aud table
CREATE TABLE tool_set_entity_aud (
  deployment_name NVARCHAR(255) NOT NULL,
  rev INTEGER NOT NULL,
  revtype SMALLINT,
  display_name NVARCHAR(255),
  description NVARCHAR(max),
  description_keywords VARBINARY(max),
  endpoint NVARCHAR(max),
  icon_url NVARCHAR(max),
  max_retry_attempts INTEGER,
  author NVARCHAR(max),
  created_at_ms BIGINT,
  updated_at_ms BIGINT,
  transport NVARCHAR(max),
  allowed_tools VARBINARY(max),
  PRIMARY KEY (rev, deployment_name)
);

ALTER TABLE tool_set_entity_aud ADD CONSTRAINT FK_REVINFO_TOOL_SET_ENTITY_AUD FOREIGN KEY (rev) REFERENCES revinfo;