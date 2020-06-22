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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.mapper.StageMapper;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;
import java.util.List;

public class StageDAO {
    public static final Logger LOG = Logger.getLoggerFor(StageDAO.class);

    protected static StageMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(StageMapper.class);
    }

    public List<Stage> all(SqlSession sqlSession, String pipelineName) {
        return mapper(sqlSession).allStages(pipelineName);
    }

    public List<Stage> stageHistory(SqlSession sqlSession, String pipelineName, String stageName, ZonedDateTime start, ZonedDateTime end) {
        return mapper(sqlSession).stageHistory(pipelineName, stageName, start, end);
    }

    public Stage One(SqlSession sqlSession, PipelineInstance pipelineInstance, String stageName, int stageCounter) {
        return mapper(sqlSession).One(pipelineInstance.getName(), pipelineInstance.getCounter(), stageName, stageCounter);
    }

    public Stage find(SqlSession sqlSession, String pipelineName, int pipelineCounter, String stageName, int stageCounter) {
        return mapper(sqlSession).One(pipelineName, pipelineCounter, stageName, stageCounter);
    }

    public void insert(SqlSession sqlSession, Stage stage) {
        mapper(sqlSession).insert(stage);
    }

    public void deleteStageRunsPriorTo(SqlSession sqlSession, ZonedDateTime scheduledDate) {
        mapper(sqlSession).deleteStageRunsPriorTo(scheduledDate);
    }

    public List<Stage> allStagesWithWorkflowIdInPipelines(SqlSession sqlSession, Long workflowID, List<String> pipelines) {
        return mapper(sqlSession).allStageInstancesWithWorkflowIdOfPipelines(workflowID, pipelines);
    }
}
