ALTER TABLE pipelines RENAME COLUMN total_time TO total_time_secs;
ALTER TABLE pipelines ALTER COLUMN total_time_secs TYPE INTEGER;
ALTER TABLE pipelines RENAME COLUMN time_waiting TO time_waiting_secs;
ALTER TABLE pipelines ALTER COLUMN time_waiting_secs TYPE INTEGER;

ALTER TABLE stages RENAME COLUMN duration TO duration_secs;
ALTER TABLE stages ALTER COLUMN duration_secs TYPE INTEGER;
ALTER TABLE stages RENAME COLUMN time_waiting TO time_waiting_secs;
ALTER TABLE stages ALTER COLUMN time_waiting_secs TYPE INTEGER;

ALTER TABLE jobs RENAME COLUMN time_waiting TO time_waiting_secs;
ALTER TABLE jobs ALTER COLUMN time_waiting_secs TYPE INTEGER;
ALTER TABLE jobs RENAME COLUMN time_building TO time_building_secs;
ALTER TABLE jobs ALTER COLUMN time_building_secs TYPE INTEGER;
ALTER TABLE jobs RENAME COLUMN duration TO duration_secs;
ALTER TABLE jobs ALTER COLUMN duration_secs TYPE INTEGER;

ALTER TABLE agent_utilization RENAME COLUMN idle_duration TO idle_duration_secs;
ALTER TABLE agent_utilization RENAME COLUMN building_duration TO building_duration_secs;
ALTER TABLE agent_utilization RENAME COLUMN cancelled_duration TO cancelled_duration_secs;
ALTER TABLE agent_utilization RENAME COLUMN lost_contact_duration TO lost_contact_duration_secs;
ALTER TABLE agent_utilization RENAME COLUMN unknown_duration TO unknown_duration_secs;