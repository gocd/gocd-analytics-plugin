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

package com.thoughtworks.gocd.analytics.executors.job;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.Job;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobBuildTimeOnAgentExecutorTest {
    @Test
    public void shouldFetchJobsDurationForAnAgent() throws Exception {
        final String pipelineName = "pipeline1";
        final String stageName = "stage1";
        final String jobName = "job1";
        final String agentUUID = "agent-UUID";

        List<Job> jobsDuration = new ArrayList<>();
        jobsDuration.add(jobFrom(pipelineName, 1, stageName, jobName, agentUUID));
        jobsDuration.add(jobFrom(pipelineName, 2, stageName, jobName, agentUUID));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_AGENT_UUID, agentUUID);
        params.put(PARAM_PIPELINE_NAME, pipelineName);
        params.put(PARAM_STAGE_NAME, stageName);
        params.put(PARAM_JOB_NAME, jobName);

        SessionFactory sessionFactory = mock(SessionFactory.class);
        JobDAO jobDao = mock(JobDAO.class);
        SqlSession session = mock(SqlSession.class);
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_DRILLDOWN, "", params);

        when(sessionFactory.openSession()).thenReturn(session);
        when(jobDao.jobDurationForAgent(session, agentUUID, pipelineName, stageName, jobName)).thenReturn(jobsDuration);

        JobBuildTimeOnAgentExecutor executor = new JobBuildTimeOnAgentExecutor(analyticsRequest, jobDao, sessionFactory);
        GoPluginApiResponse response = executor.execute();
        String expectedResponse = "{\n" +
                "    \"data\": \"{\\\"identifier\\\":\\\"pipeline1/stage1/job1\\\",\\\"agent_uuid\\\":\\\"agent-UUID\\\",\\\"jobs\\\":[{\\\"pipeline_name\\\":\\\"pipeline1\\\",\\\"pipeline_counter\\\":1,\\\"stage_name\\\":\\\"stage1\\\",\\\"stage_counter\\\":0,\\\"job_name\\\":\\\"job1\\\",\\\"time_waiting_secs\\\":0,\\\"time_building_secs\\\":0,\\\"duration_secs\\\":0,\\\"agent_uuid\\\":\\\"agent-UUID\\\"},{\\\"pipeline_name\\\":\\\"pipeline1\\\",\\\"pipeline_counter\\\":2,\\\"stage_name\\\":\\\"stage1\\\",\\\"stage_counter\\\":0,\\\"job_name\\\":\\\"job1\\\",\\\"time_waiting_secs\\\":0,\\\"time_building_secs\\\":0,\\\"duration_secs\\\":0,\\\"agent_uuid\\\":\\\"agent-UUID\\\"}]}\",\n" +
                "    \"view_path\": \"job_build_time_on_an_agent.html\"\n" +
                "}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
        assertEquals(200, response.responseCode());
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, String jobName, String agentUUID) {
        Job job = new Job();

        job.setPipelineName(pipelineName);
        job.setPipelineCounter(pipelineCounter);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setAgentUuid(agentUUID);

        return job;
    }
}
