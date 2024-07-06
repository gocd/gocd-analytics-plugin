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
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.*;
import static com.thoughtworks.gocd.analytics.AvailableAnalytics.JOBS_WITH_THE_HIGHEST_WAIT_TIME;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobsHighestWaitTimeExecutorTest {
    @Test
    public void shouldFetchLongestWaitingJobs() throws Exception {
        final String startDate = "2018-01-01";
        final String endDate = "2018-01-02";

        SessionFactory sessionFactory = mock(SessionFactory.class);
        JobDAO jobDAO = mock(JobDAO.class);
        SqlSession session = mock(SqlSession.class);
        when(sessionFactory.openSession()).thenReturn(session);

        List<Job> jobs = new ArrayList<>();
        jobs.add(testJob(1, "job1"));
        jobs.add(testJob(1, "job2"));

        when(jobDAO.longestWaitingFor(
                session,
                "pip",
                DateUtils.parseDate(startDate).toString(),
                DateUtils.parseDate(endDate).toString(),
                10
        )).thenReturn(jobs);

        final HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_PIPELINE_NAME, "pip");
        params.put(PARAM_START_DATE, startDate);
        params.put(PARAM_END_DATE, endDate);

        final AnalyticsRequest request = new AnalyticsRequest(
                JOBS_WITH_THE_HIGHEST_WAIT_TIME.getType(),
                JOBS_WITH_THE_HIGHEST_WAIT_TIME.getId(),
                params
        );

        JobsHighestWaitTimeExecutor executor = new JobsHighestWaitTimeExecutor(request, jobDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertEquals(200, response.responseCode());

        Map<String, Object> resp = new HashMap<>();
        resp.put(PARAM_PIPELINE_NAME, "pip");
        resp.put(PARAM_START_DATE, formatInUTC(startDate));
        resp.put(PARAM_END_DATE, formatInUTC(endDate));
        resp.put("jobs", jobs);

        String expectedResponse = "{\"data\":" + GSON.toJson(GSON.toJson(resp)) + ",\"view_path\":\"job-chart.html\"}";
        assertEquals(expectedResponse, response.responseBody());
    }

    private String formatInUTC(String startDate) {
        return DateUtils.parseDate(startDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC")));
    }

    private Job testJob(int timeWaiting, String jobName) {
        final Job job = new Job();
        job.setJobName(jobName);
        job.setTimeWaitingSecs(timeWaiting);
        job.setPipelineCounter(1);
        job.setStageCounter(1);
        return job;
    }
}
