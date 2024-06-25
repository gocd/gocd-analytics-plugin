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
import com.thoughtworks.gocd.analytics.mapper.PipelineMapper;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.PipelineStateSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummaryDetails;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummaryTwo;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;
import java.util.List;

public class PipelineDAO {
    public static final Logger LOG = Logger.getLoggerFor(PipelineDAO.class);

    public List<PipelineInstance> instancesForPipeline(SqlSession sqlSession, String pipelineName, ZonedDateTime start, ZonedDateTime end) {
        return mapper(sqlSession).allPipelineInstancesFor(pipelineName, start, end);
    }

    public List<Pipeline> allPipelines(SqlSession sqlSession) {
        return mapper(sqlSession).allPipelines();
    }

    public PipelineTimeSummary pipelineSummary(SqlSession sqlSession) {
        return mapper(sqlSession).pipelineSummary();
    }

    public List<PipelineTimeSummaryTwo> pipelineSummaryTwo(SqlSession sqlSession,
        String startDate, String endDate, String result) {
        return mapper(sqlSession).pipelineSummaryTwo(startDate, endDate, result);
    }

    public List<PipelineStateSummary> pipelineStateSummary(SqlSession sqlSession) {
        return mapper(sqlSession).pipelineStateSummary();
    }

    public void updateOrInsert(SqlSession sqlSession, PipelineInstance pipeline) {
        PipelineMapper mapper = mapper(sqlSession);
        PipelineInstance existingInstance = mapper.findInstance(pipeline.getName(), pipeline.getCounter());
        if (existingInstance != null) {
            pipeline.setId(existingInstance.getId());
            mapper.update(pipeline);
        } else {
            mapper.insert(pipeline);
        }
    }

    public List<Pipeline> longestWaiting(SqlSession sqlSession, ZonedDateTime start, ZonedDateTime end, int limit) {
        return mapper(sqlSession).longestWaiting(start, end, limit);
    }

    public void insertEach(SqlSession sqlSession, List<PipelineInstance> pipelineInstances) {
        for (PipelineInstance pipelineInstance : pipelineInstances) {
            mapper(sqlSession).insert(pipelineInstance);
        }
    }

    public PipelineInstance find(SqlSession sqlSession, String name, int counter) {
        return mapper(sqlSession).find(name, counter);
    }

    public List<PipelineInstance> allPipelineInstancesWithNameIn(SqlSession sqlSession, String workflowSource, List<String> pipelines) {
        return mapper(sqlSession).allPipelineInstancesWithNameIn(workflowSource, pipelines);
    }

    public List<PipelineInstance> allPipelineWithNameAndCounter(SqlSession sqlSession,
        String pipelineName) {
        return mapper(sqlSession).allPipelineWithNameAndCounter(pipelineName);
    }

    public List<PipelineInstance> pipelineSummaryDetails(SqlSession sqlSession,
        String pipeline_name, String result) {
        return mapper(sqlSession).pipelineSummaryDetails(pipeline_name, result);
    }

    private PipelineMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(PipelineMapper.class);
    }
}
