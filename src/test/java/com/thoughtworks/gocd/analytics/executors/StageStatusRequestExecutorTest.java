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

package com.thoughtworks.gocd.analytics.executors;

import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.MaterialRevisionDAO;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.executors.workflow.WorkflowAllocatorFactory;
import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.models.MaterialRevision;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthState;
import com.thoughtworks.gocd.analytics.utils.Builder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.MaterialRevisionMother.materialRevisionFrom;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class StageStatusRequestExecutorTest {

    private JobDAO jobDAO;
    private StageDAO stageDAO;
    private PipelineDAO pipelineDAO;
    private SqlSession sqlSession;
    private SessionFactory sessionFactory;
    private GoPluginApiRequest apiRequest;
    private Builder builder;
    private PluginHealthMessageService pluginHealthMessageService;
    private WorkflowAllocatorFactory workflowAllocatorFactory;
    private MaterialRevisionDAO materialRevisionDAO;

    @Before
    public void setUp() throws Exception {
        apiRequest = mock(GoPluginApiRequest.class);
        jobDAO = mock(JobDAO.class);
        stageDAO = mock(StageDAO.class);
        pipelineDAO = mock(PipelineDAO.class);
        sqlSession = mock(SqlSession.class);
        sessionFactory = mock(SessionFactory.class);
        builder = mock(Builder.class);
        workflowAllocatorFactory = mock(WorkflowAllocatorFactory.class);
        pluginHealthMessageService = mock(PluginHealthMessageService.class);
        materialRevisionDAO = mock(MaterialRevisionDAO.class);
    }

    @Test
    public void shouldNotInsertJobsForAInCompleteStage() throws Exception {
        when(apiRequest.requestBody()).thenReturn(stageJson("1", "Building"));
        when(sessionFactory.openSession()).thenReturn(sqlSession);

        StageStatusRequestExecutor executor = new StageStatusRequestExecutor(apiRequest, jobDAO, stageDAO, pipelineDAO,
                null, new Builder(), sessionFactory, pluginHealthMessageService, workflowAllocatorFactory);

        GoPluginApiResponse response = executor.execute();

        assertSuccess(response);
        verifyZeroInteractions(jobDAO);
        verifyZeroInteractions(stageDAO);
        verifyZeroInteractions(pipelineDAO);
    }

    @Test
    public void shouldInsertPipelineStageAndJobForACompletedStage() throws Exception {
        List<Job> jobs = Collections.singletonList(mock(Job.class));
        Stage stage = mock(Stage.class);
        PipelineInstance pipelineInstance = mock(PipelineInstance.class);
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(apiRequest.requestBody()).thenReturn(stageJson("1", "Passed"));
        when(builder.buildJobs(any())).thenReturn(jobs);

        when(builder.buildStage(any())).thenReturn(stage);
        when(builder.buildPipelineInstance(any())).thenReturn(pipelineInstance);

        StageStatusRequestExecutor executor = new StageStatusRequestExecutor(apiRequest, jobDAO, stageDAO, pipelineDAO,
                null, builder, sessionFactory, pluginHealthMessageService, workflowAllocatorFactory);

        GoPluginApiResponse response = executor.execute();

        assertSuccess(response);
        verify(jobDAO).insertJobs(sqlSession, jobs);
        verify(stageDAO).insert(sqlSession, stage);
        verify(pipelineDAO).updateOrInsert(sqlSession, pipelineInstance);
    }

    @Test
    public void shouldAccountStageReRunWhileUpdatingPipelines() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        PipelineInstance pipelineInstance = new PipelineInstance(1, "pipeline-name", 10, 20, "passed", now, now);
        Stage stageWithCounter1 = stageWith("stage-name", 1, 10, 5);
        Stage stageWithCounter2 = stageWith("stage-name", 2, 20, 10);
        List<Job> jobs = Collections.singletonList(mock(Job.class));

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(apiRequest.requestBody()).thenReturn(stageJson("2", "Passed"));

        when(builder.buildPipelineInstance(any())).thenReturn(pipelineInstance);

        when(stageDAO.One(sqlSession, pipelineInstance, "stage-name", 1)).thenReturn(stageWithCounter1);
        when(builder.buildStage(any())).thenReturn(stageWithCounter2);

        when(builder.buildJobs(any())).thenReturn(jobs);

        StageStatusRequestExecutor executor = new StageStatusRequestExecutor(apiRequest, jobDAO, stageDAO, pipelineDAO,
                null, builder, sessionFactory, pluginHealthMessageService, workflowAllocatorFactory);

        GoPluginApiResponse response = executor.execute();

        assertSuccess(response);
        verify(jobDAO).insertJobs(sqlSession, jobs);
        verify(stageDAO).insert(sqlSession, stageWithCounter2);
        verify(pipelineDAO).updateOrInsert(sqlSession, new PipelineInstance(1, "pipeline-name", 5, 10, "passed", now, now));
    }

    @Test
    public void shouldNotifyGoCDWithPluginHealthMessageInAbsenceOfDBConnection() throws Exception {
        when(apiRequest.requestBody()).thenReturn(stageJson("1", "Passed"));

        StageStatusRequestExecutor executor = new StageStatusRequestExecutor(apiRequest, jobDAO, stageDAO, pipelineDAO,
                null, new Builder(), null, pluginHealthMessageService, workflowAllocatorFactory);

        GoPluginApiResponse response = executor.execute();

        assertSuccess(response);
        verify(pluginHealthMessageService).update(any(PluginHealthState.class));
        verifyZeroInteractions(jobDAO);
        verifyZeroInteractions(stageDAO);
        verifyZeroInteractions(pipelineDAO);
    }

    @Test
    public void shouldInsertSCMMaterialsResponsibleForTheBuild() throws Exception {
        ZonedDateTime buildTriggerTime = ZonedDateTime.now();
        MaterialRevision scmMaterial = materialRevisionFrom(-1, "material_fingerprint",
                "1", "git", buildTriggerTime);
        MaterialRevision dependencyMaterial = materialRevisionFrom(-1, "material_fingerprint",
                "pipeline-name/1/stage-name/1", "pipeline", buildTriggerTime);
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(apiRequest.requestBody()).thenReturn(stageJson("1", "Passed"));
        when(builder.materialRevisionsResponsibleForTheBuild(any())).thenReturn(Arrays.asList(scmMaterial, dependencyMaterial));

        StageStatusRequestExecutor executor = new StageStatusRequestExecutor(apiRequest, jobDAO, stageDAO, pipelineDAO,
                materialRevisionDAO, builder, sessionFactory, pluginHealthMessageService, workflowAllocatorFactory);

        GoPluginApiResponse response = executor.execute();

        assertSuccess(response);

        verify(materialRevisionDAO).insert(sqlSession, scmMaterial);
        verify(materialRevisionDAO, never()).insert(sqlSession, dependencyMaterial);

    }

    public void assertSuccess(GoPluginApiResponse response) {
        Map<String, String> responseJson = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());

        assertThat(response.responseCode(), is(200));
        assertThat(responseJson.get("status"), is("success"));
    }

    private Stage stageWith(String stageName, int stageCounter, int timeWaiting, int duration) {
        Stage stage = new Stage(stageName, stageCounter);
        stage.setTimeWaitingSecs(timeWaiting);
        stage.setTotalTimeSecs(duration);

        return stage;
    }

    private String stageJson(String counter, String state) {
        return String.format("{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"%s\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"%s\",\n" +
                "      \"result\": \"Unkown\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100Z\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:39.100Z\",\n" +
                "          \"assign-time\": \"2011-07-13T19:43:38.100Z\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}", counter, state);
    }
}
