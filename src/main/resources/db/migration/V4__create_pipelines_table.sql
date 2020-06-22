create table pipelines (id serial PRIMARY KEY,
  name varchar(225),
  counter int,
  result varchar(225),
  total_time bigint,
  time_waiting bigint
);