CREATE TABLE pipeline_workflows (
  id SERIAL PRIMARY KEY,
  pipeline_id BIGINT REFERENCES pipelines(id),
  stage_id BIGINT REFERENCES stages(id),
  material_revision_id BIGINT REFERENCES material_revisions(id),
  workflow_id BIGINT REFERENCES workflows(id) NOT NULL
);