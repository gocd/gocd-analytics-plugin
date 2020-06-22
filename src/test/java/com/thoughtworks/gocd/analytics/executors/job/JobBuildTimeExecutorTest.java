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
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.*;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobBuildTimeExecutorTest {
    @Test
    public void shouldFetchJobHistory() throws Exception {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        final String pipeline = "pipeline";
        final String stage = "stage";
        final String jobName = "test_job";

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_PIPELINE_NAME, pipeline);
        params.put(PARAM_STAGE_NAME, stage);
        params.put(PARAM_JOB_NAME, jobName);
        AnalyticsRequest request = new AnalyticsRequest(TYPE_JOB, "", params);

        JobDAO jobDAO = mock(JobDAO.class);
        SqlSession session = mock(SqlSession.class);
        when(sessionFactory.openSession()).thenReturn(session);

        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob(jobName));
        jobs.add(testJob(jobName));
        when(jobDAO.jobHistory(session, pipeline, stage, jobName)).thenReturn(jobs);

        JobBuildTimeExecutor executor = new JobBuildTimeExecutor(request, jobDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));

        Map<String, Object> data = new HashMap<>();
        data.put("identifier", pipeline + "/" + stage + "/" + jobName);
        data.put("jobs", jobs);

        String expectedResponse = "{\"data\":" + GSON.toJson(GSON.toJson(data)) + ",\"view_path\":\"job-history.html\"}";
        assertEquals(expectedResponse, response.responseBody());
    }

    private Job testJob(String jobName) {
        final Job job = new Job();
        job.setJobName(jobName);
        job.setPipelineCounter(1);
        job.setStageCounter(1);
        return job;
    }
}
