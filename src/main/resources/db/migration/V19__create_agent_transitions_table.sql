CREATE TABLE agent_transitions (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(255) REFERENCES agents(uuid),
  agent_config_state CITEXT NOT NULL,
  agent_state CITEXT NOT NULL,
  build_state CITEXT NOT NULL,
  transition_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_agent_transitions_agent_uuid ON agent_transitions(uuid);
