ALTER TABLE stages RENAME COLUMN created_at TO scheduled_at;
ALTER TABLE stages RENAME COLUMN last_transition_time TO completed_at;
ALTER TABLE stages ADD COLUMN time_waiting bigint;
