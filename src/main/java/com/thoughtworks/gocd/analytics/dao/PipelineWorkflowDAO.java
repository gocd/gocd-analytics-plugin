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

package com.thoughtworks.gocd.analytics.dao;

import com.thoughtworks.gocd.analytics.mapper.PipelineWorkflowMapper;
import com.thoughtworks.gocd.analytics.models.PipelineWorkflow;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class PipelineWorkflowDAO {
    public void insert(SqlSession sqlSession, long pipelineId, long stageId, long workflowId) {
        mapper(sqlSession).insertPipelineWorkflow(pipelineId, stageId, workflowId);
    }

    public void insert(SqlSession sqlSession, long materialRevisionId, long workflowId) {
         mapper(sqlSession).insertMaterialWorkflow(materialRevisionId, workflowId);
    }

    public List<PipelineWorkflow> workflowsFor(SqlSession sqlSession, long pipelineId, long stageId) {
        return mapper(sqlSession).workflowsFor(pipelineId, stageId);
    }

    public PipelineWorkflow workflowFor(SqlSession sqlSession, long materialRevisionId) {
        return mapper(sqlSession).workflowFor(materialRevisionId);
    }

    private PipelineWorkflowMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(PipelineWorkflowMapper.class);
    }
}
