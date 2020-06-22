ALTER TABLE pipeline_workflows DROP CONSTRAINT IF EXISTS pipeline_workflows_stage_id_fkey;

ALTER TABLE pipeline_workflows ADD CONSTRAINT pipeline_workflows_stage_id_fkey FOREIGN KEY (stage_id) REFERENCES stages(id) ON DELETE CASCADE;
