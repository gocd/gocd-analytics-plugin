CREATE SEQUENCE IF NOT EXISTS pipelines_id_seq START WITH 1;
ALTER TABLE pipelines ALTER COLUMN ID TYPE bigint;
ALTER TABLE pipelines ALTER COLUMN ID SET DEFAULT nextval('pipelines_id_seq'::regclass);

CREATE SEQUENCE IF NOT EXISTS stages_id_seq START WITH 1;
ALTER TABLE stages ALTER COLUMN ID TYPE bigint;
ALTER TABLE stages ALTER COLUMN ID SET DEFAULT nextval('stages_id_seq'::regclass);

CREATE SEQUENCE IF NOT EXISTS jobs_id_seq START WITH 1;
ALTER TABLE jobs ALTER COLUMN ID TYPE bigint;
ALTER TABLE jobs ALTER COLUMN ID SET DEFAULT nextval('jobs_id_seq'::regclass);

CREATE SEQUENCE IF NOT EXISTS agents_id_seq START WITH 1;
ALTER TABLE agents ALTER COLUMN ID TYPE bigint;
ALTER TABLE agents ALTER COLUMN ID SET DEFAULT nextval('agents_id_seq'::regclass);

CREATE SEQUENCE IF NOT EXISTS agent_utilization_id_seq START WITH 1;
ALTER TABLE agent_utilization ALTER COLUMN ID TYPE bigint;
ALTER TABLE agent_utilization ALTER COLUMN ID SET DEFAULT nextval('agent_utilization_id_seq'::regclass);