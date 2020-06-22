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

package com.thoughtworks.gocd.analytics.executors.pipeline;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_END_DATE;
import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_START_DATE;
import static com.thoughtworks.gocd.analytics.AvailableAnalytics.PIPELINES_WITH_THE_HIGHEST_WAIT_TIME;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelinesHighestWaitTimeExecutorTest {
    @Test
    public void shouldFetchLongestWaitingPipelines() throws Exception {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        PipelineDAO pipelineDAO = mock(PipelineDAO.class);
        SqlSession session = mock(SqlSession.class);
        when(sessionFactory.openSession()).thenReturn(session);

        List<Pipeline> pipelines = new ArrayList<>();
        pipelines.add(testPipeline(1, 1, "job1"));
        pipelines.add(testPipeline(1, 1, "job2"));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_START_DATE, "2018-03-15");
        params.put(PARAM_END_DATE, "2018-03-22");
        AnalyticsRequest request = new AnalyticsRequest(PIPELINES_WITH_THE_HIGHEST_WAIT_TIME.getType(), PIPELINES_WITH_THE_HIGHEST_WAIT_TIME.getId(), params);
        when(pipelineDAO.longestWaiting(eq(session), any(ZonedDateTime.class), any(ZonedDateTime.class), eq(10))).thenReturn(pipelines);

        PipelinesHighestWaitTimeExecutor executor = new PipelinesHighestWaitTimeExecutor(request, pipelineDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        String expectedResponse = "{\"data\":" + GSON.toJson(GSON.toJson(pipelines)) + ",\"view_path\":\"longest-waiting-pipelines-chart.html\"}";
        assertEquals(expectedResponse, response.responseBody());
    }

    private Pipeline testPipeline(int avgTimeWaiting, int avgTimeBuilding, String name) {
        final Pipeline pipeline = new Pipeline();
        pipeline.setName(name);
        pipeline.setAvgWaitTimeSecs(avgTimeWaiting);
        pipeline.setAvgBuildTimeSecs(avgTimeBuilding);
        return pipeline;
    }
}
