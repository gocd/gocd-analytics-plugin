/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.gocd.analytics.mapper;

import com.thoughtworks.gocd.analytics.models.PipelineWorkflow;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PipelineWorkflowMapper {
    @Insert("INSERT INTO pipeline_workflows " +
            "(stage_id," +
            "pipeline_id," +
            "workflow_id) VALUES " +
            "(#{stageId}," +
            "#{pipelineId}," +
            "#{workflowId});"
    )
    void insertPipelineWorkflow(@Param("pipelineId") long pipelineId, @Param("stageId") long stageId, @Param("workflowId") long workflowId);

    @Insert("INSERT INTO pipeline_workflows " +
            "(material_revision_id," +
            "workflow_id) VALUES " +
            "(#{materialRevisionId}," +
            "#{workflowId});"
    )
    void insertMaterialWorkflow(@Param("materialRevisionId") long materialRevisionId, @Param("workflowId") long workflowId);

    @Results(id = "PipelineWorkflow", value = {
            @Result(property = "pipelineId", column = "pipeline_id"),
            @Result(property = "stageId", column = "stage_id"),
            @Result(property = "materialRevisionId", column = "material_revision_id"),
            @Result(property = "workflowId", column = "workflow_id"),
    })
    @Select("SELECT * FROM pipeline_workflows \n" +
            "  WHERE pipeline_id = #{pipelineId} \n" +
            "  AND stage_id = #{stageId};"
    )
    List<PipelineWorkflow> workflowsFor(@Param("pipelineId") long pipelineId, @Param("stageId") long stageId);

    @ResultMap("PipelineWorkflow")
    @Select("SELECT * FROM pipeline_workflows \n" +
            "  WHERE material_revision_id = #{materialRevisionId} \n" +
            "  LIMIT 1;"
    )
    PipelineWorkflow workflowFor(@Param("materialRevisionId") long materialRevisionId);
}
