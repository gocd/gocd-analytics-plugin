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

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.thoughtworks.gocd.analytics.StageMother.stageWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class StageStatusRequestExecutorTest {
    private TestDBConnectionManager manager;
    private PipelineDAO pipelineDAO;
    private StageDAO stageDAO;
    private JobDAO jobDAO;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        pipelineDAO = new PipelineDAO();
        stageDAO = new StageDAO();
        jobDAO = new JobDAO();
        manager = new TestDBConnectionManager();
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void onStageStatusRequest_shouldCreatePipelineStageAndJobsRecords() throws Exception {
        DefaultGoPluginApiRequest apiRequest = new DefaultGoPluginApiRequest("api", "1.0", "stage-status");
        apiRequest.setRequestBody(stageJson());

        new StageStatusRequestExecutor(apiRequest, manager.getSessionFactory(), mock(PluginHealthMessageService.class)).execute();

        try (SqlSession sqlSession = manager.getSessionFactory().openSession()) {
            List<PipelineInstance> pipelineInstances = pipelineDAO.instancesForPipeline(sqlSession, "pipeline-name", null, ZonedDateTime.now());

            assertEquals(1, pipelineInstances.size());
            PipelineInstance instance = pipelineInstances.get(0);
            assertEquals(1, instance.getCounter());
            assertEquals("Passed", instance.getResult());
            assertEquals(2, instance.getTotalTimeSecs());
            assertEquals(1, instance.getTimeWaitingSecs());

            List<Stage> stages = stageDAO.all(sqlSession, "pipeline-name");

            assertEquals(1, stages.size());
            Stage expected = stageWith("pipeline-name", 1, "stage-name", 1,
                    "Passed", "Passed", 2, toZondedDateTimeInUTC("2018-04-13T14:25:29.165+0000"),
                    "success", "changes", toZondedDateTimeInUTC("2018-04-13T14:25:31.165+0000"), 1);
            assertEquals(expected, stages.get(0));

            List<Job> jobs = jobDAO.all(sqlSession, "pipeline-name");

            assertEquals(1, jobs.size());
            Job job = jobFrom("pipeline-name", 1, "stage-name", 1,
                    "job-name", "Passed", toZondedDateTimeInUTC("2018-04-13T14:25:29.165+0000"),
                    toZondedDateTimeInUTC("2018-04-13T14:25:31.165+0000"), toZondedDateTimeInUTC("2018-04-13T14:25:30.165+0000"),
                    1, 1, 2, "uuid");
            assertEquals(job, jobs.get(0));
        }
    }

    private ZonedDateTime toZondedDateTimeInUTC(String time) {
        return ZonedDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter, String jobName,
                        String result, ZonedDateTime scheduledAt, ZonedDateTime completedAt, ZonedDateTime assignedAt,
                        int timeWaiting, int timeBuilding, int duration, String agentUuid) {
        Job job = new Job();
        job.setPipelineName(pipelineName);
        job.setPipelineCounter(pipelineCounter);
        job.setStageName(stageName);
        job.setStageCounter(stageCounter);
        job.setJobName(jobName);
        job.setResult(result);
        job.setScheduledAt(scheduledAt);
        job.setCompletedAt(completedAt);
        job.setAssignedAt(assignedAt);
        job.setTimeWaitingSecs(timeWaiting);
        job.setTimeBuildingSecs(timeBuilding);
        job.setDurationSecs(duration);
        job.setAgentUuid(agentUuid);

        return job;
    }

    private String stageJson() {
        return "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2018-04-13T14:25:29.165+0000\",\n" +
                "      \"last-transition-time\": \"2018-04-13T14:25:31.165+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2018-04-13T14:25:29.165+0000\",\n" +
                "          \"complete-time\": \"2018-04-13T14:25:31.165+0000\",\n" +
                "          \"assign-time\": \"2018-04-13T14:25:30.165+0000\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
