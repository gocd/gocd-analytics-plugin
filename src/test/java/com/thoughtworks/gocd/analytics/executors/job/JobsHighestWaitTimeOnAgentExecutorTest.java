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
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobsHighestWaitTimeOnAgentExecutorTest {
    private ArgumentCaptor<ZonedDateTime> endDate;
    private ArgumentCaptor<ZonedDateTime> startDate;

    @BeforeEach
    public void setUp() throws Exception {
        endDate = ArgumentCaptor.forClass(ZonedDateTime.class);
        startDate = ArgumentCaptor.forClass(ZonedDateTime.class);
    }

    @Test
    public void shouldFetchJobsWithLongestWaitTimeForAnAgent() throws Exception {
        final String agentUUID = "agent-uuid";
        final String startDate = "2018-03-15";
        final String endDate = "2018-03-22";
        final int limit = 10;

        List<Job> jobsWithLongestWaitTime = new ArrayList<>();
        jobsWithLongestWaitTime.add(jobFrom("pipeline1", "stage1", "job1", 10));
        jobsWithLongestWaitTime.add(jobFrom("pipeline2", "stage1", "job1", 5));

        SessionFactory sessionFactory = mock(SessionFactory.class);
        JobDAO jobDao = mock(JobDAO.class);
        SqlSession session = mock(SqlSession.class);
        AnalyticsRequest analyticsRequest = mock(AnalyticsRequest.class);

        when(sessionFactory.openSession()).thenReturn(session);

        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_AGENT_UUID, agentUUID);
        params.put(PARAM_START_DATE, startDate);
        params.put(PARAM_END_DATE, endDate);

        when(analyticsRequest.getParams()).thenReturn(params);
        when(jobDao.longestWaitingJobsForAgent(eq(session), eq(agentUUID), this.startDate.capture(), this.endDate.capture(), eq(limit))).thenReturn(jobsWithLongestWaitTime);

        JobsHighestWaitTimeOnAgentExecutor executor = new JobsHighestWaitTimeOnAgentExecutor(analyticsRequest, jobDao, sessionFactory);
        GoPluginApiResponse response = executor.execute();
        String expectedResponse = "{\n" +
                "    \"data\": \"{\\\"agent_uuid\\\":\\\"agent-uuid\\\",\\\"jobs\\\":[{\\\"pipeline_name\\\":\\\"pipeline1\\\",\\\"pipeline_counter\\\":0,\\\"stage_name\\\":\\\"stage1\\\",\\\"stage_counter\\\":0,\\\"job_name\\\":\\\"job1\\\",\\\"time_waiting_secs\\\":10,\\\"time_building_secs\\\":0,\\\"duration_secs\\\":0},{\\\"pipeline_name\\\":\\\"pipeline2\\\",\\\"pipeline_counter\\\":0,\\\"stage_name\\\":\\\"stage1\\\",\\\"stage_counter\\\":0,\\\"job_name\\\":\\\"job1\\\",\\\"time_waiting_secs\\\":5,\\\"time_building_secs\\\":0,\\\"duration_secs\\\":0}]}\",\n" +
                "    \"view_path\": \"jobs_with_the_highest_wait_time_on_an_agent_chart.html\"\n" +
                "}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);

        assertEquals(DateUtils.parseDate(startDate), this.startDate.getValue());
        assertEquals(DateUtils.parseDate(endDate), this.endDate.getValue());
        assertEquals(200, response.responseCode());
    }

    private Job jobFrom(String pipelineName, String stageName, String jobName, int timeWaitingSecs) {
        Job job = new Job();

        job.setPipelineName(pipelineName);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setTimeWaitingSecs(timeWaitingSecs);

        return job;
    }
}
