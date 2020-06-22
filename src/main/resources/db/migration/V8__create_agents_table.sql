CREATE TABLE agents (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(255) UNIQUE NOT NULL,
  host_name VARCHAR(255),
  is_elastic BOOLEAN,
  ip_address VARCHAR(255),
  operating_system VARCHAR(255),
  free_space VARCHAR(255),
  config_state CITEXT,
  created_at TIMESTAMP WITH TIME ZONE
);