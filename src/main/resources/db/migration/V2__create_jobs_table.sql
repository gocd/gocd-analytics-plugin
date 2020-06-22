create table jobs (id serial PRIMARY KEY,
  pipeline_name varchar(225),
  pipeline_counter int,
  stage_name varchar(225),
  stage_counter int,
  job_name varchar(225),
  result varchar(225),
  scheduled_at timestamp,
  completed_at timestamp,
  assigned_at timestamp,
  time_waiting bigint,
  time_building bigint,
  duration bigint
);