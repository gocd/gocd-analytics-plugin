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
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_PIPELINE_NAME;
import static com.thoughtworks.gocd.analytics.AnalyticTypes.TYPE_PIPELINE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelineBuildTimeExecutorTest {

    private static final String TEST_PIPELINE_NAME = "test";
    private SessionFactory sessionFactory;
    private SqlSession sqlSession;
    private PipelineDAO pipelineDAO;

    @Before
    public void setUp() throws Exception {
        sessionFactory = mock(SessionFactory.class);
        sqlSession = mock(SqlSession.class);
        pipelineDAO = mock(PipelineDAO.class);
    }

    @Test
    public void shouldFetchPipelineAnalytics() throws Exception {
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_PIPELINE, "", Collections.singletonMap(PARAM_PIPELINE_NAME, TEST_PIPELINE_NAME));

        ZonedDateTime scheduled_at = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime ltt = ZonedDateTime.now(ZoneId.of("UTC"));

        final List<PipelineInstance> instances = Collections.singletonList(new PipelineInstance(10, TEST_PIPELINE_NAME, 111, 15, "Passed",
                scheduled_at,
                ltt));

        PipelineBuildTimeExecutor executor = new PipelineBuildTimeExecutor(analyticsRequest, pipelineDAO, sessionFactory);
        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(pipelineDAO.instancesForPipeline(eq(sqlSession), eq(TEST_PIPELINE_NAME), any(), any())).thenReturn(instances);

        GoPluginApiResponse response = executor.execute();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DefaultZonedDateTimeTypeAdapter.DATE_PATTERN);
        assertThat(response.responseCode(), is(200));

        String expectedResponse = "{" +
                "\"data\": \"{\\\"name\\\":\\\"test\\\"," +
                "\\\"instances\\\":[{" +
                "\\\"counter\\\":10," +
                "\\\"name\\\":\\\"test\\\"," +
                "\\\"total_time_secs\\\":111," +
                "\\\"time_waiting_secs\\\":15," +
                "\\\"scheduled_at\\\":\\\"" + scheduled_at.withZoneSameInstant(ZoneId.of("UTC")).format(fmt) + "\\\"," +
                "\\\"last_transition_time\\\":\\\"" + ltt.format(fmt) + "\\\"," +
                "\\\"result\\\":\\\"Passed\\\"}]}\"," +
                "\"view_path\": \"pipeline-instances-chart.html\"" +
                "}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }

    @Test
    public void shouldRespondNormallyInAbsenceOfDataForAPipeline() throws Exception {
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_PIPELINE, "", Collections.singletonMap(PARAM_PIPELINE_NAME, TEST_PIPELINE_NAME));

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(pipelineDAO.instancesForPipeline(eq(sqlSession), eq(TEST_PIPELINE_NAME), eq(null), eq(ZonedDateTime.now()))).thenReturn(Collections.emptyList());

        PipelineBuildTimeExecutor executor = new PipelineBuildTimeExecutor(analyticsRequest, pipelineDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));

        String expectedResponse = "{" +
                "\"data\": \"{\\\"name\\\":\\\"test\\\"," +
                "\\\"instances\\\":[]" +
                "}\"," +
                "\"view_path\": \"pipeline-instances-chart.html\"" +
                "}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }
}
