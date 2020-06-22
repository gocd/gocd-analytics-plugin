CREATE TABLE agent_utilization (
  id SERIAL PRIMARY KEY,
  uuid VARCHAR(255) REFERENCES agents(uuid),
  idle_duration INTEGER DEFAULT 0,
  building_duration INTEGER DEFAULT 0,
  cancelled_duration INTEGER DEFAULT 0,
  lost_contact_duration INTEGER DEFAULT 0,
  unknown_duration INTEGER DEFAULT 0,
  last_known_state CITEXT NOT NULL,
  utilization_date TIMESTAMP WITH TIME ZONE NOT NULL,
  last_transition_time TIMESTAMP WITH TIME ZONE NOT NULL
);