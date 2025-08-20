-- Create tool_set_entity table
CREATE TABLE tool_set_entity (
  deployment_name VARCHAR(255) NOT NULL,
  display_name TEXT,
  description TEXT,
  description_keywords TEXT ARRAY,
  endpoint TEXT,
  icon_url TEXT,
  max_retry_attempts INTEGER,
  author TEXT,
  created_at_ms BIGINT NOT NULL,
  updated_at_ms BIGINT NOT NULL,
  transport TEXT,
  allowed_tools TEXT ARRAY,
  PRIMARY KEY (deployment_name)
);

ALTER TABLE tool_set_entity ADD CONSTRAINT FK_TOOL_SET_ENTITY_DEPLOYMENT_NAME FOREIGN KEY (deployment_name) REFERENCES deployment_entity (name);

-- Create tool_set_entity_aud table
CREATE TABLE tool_set_entity_aud (
  deployment_name VARCHAR(255) NOT NULL,
  rev INTEGER NOT NULL,
  revtype SMALLINT,
  display_name TEXT,
  description TEXT,
  description_keywords TEXT ARRAY,
  endpoint TEXT,
  icon_url TEXT,
  max_retry_attempts INTEGER,
  author TEXT,
  created_at_ms BIGINT,
  updated_at_ms BIGINT,
  transport TEXT,
  allowed_tools TEXT ARRAY,
  PRIMARY KEY (rev, deployment_name)
);

ALTER TABLE IF EXISTS tool_set_entity_aud ADD CONSTRAINT FK_REVINFO_TOOL_SET_ENTITY_AUD FOREIGN KEY (rev) REFERENCES revinfo;