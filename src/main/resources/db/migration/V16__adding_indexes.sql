CREATE INDEX idx_pipelines_name_counter ON pipelines(name,counter);

CREATE INDEX idx_stages_pipeline_name_counter_stage_name_counter ON stages(pipeline_name,pipeline_counter,stage_name,stage_counter);

CREATE INDEX idx_jobs_pipeline_name_stage_name_job_name ON jobs(pipeline_name,stage_name,job_name);

CREATE INDEX idx_agent_utilization_uuid ON agent_utilization(uuid);