CREATE TABLE material_revisions (
  id SERIAL PRIMARY KEY,
  fingerprint VARCHAR(255),
  revision VARCHAR(255),
  build_schedule_time TIMESTAMP WITH TIME ZONE,
  UNIQUE (fingerprint, revision)
);