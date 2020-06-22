create table stages (id serial PRIMARY KEY,
  pipeline_name varchar(225),
  pipeline_counter int,
  stage_name varchar(225),
  stage_counter int,
  result varchar(225),
  state varchar(225),
  approval_type varchar(225),
  approved_by varchar(225),
  created_at timestamp,
  last_transition_time timestamp,
  duration bigint
);
