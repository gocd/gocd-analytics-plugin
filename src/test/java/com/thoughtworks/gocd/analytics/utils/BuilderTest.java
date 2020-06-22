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

package com.thoughtworks.gocd.analytics.utils;

import com.thoughtworks.gocd.analytics.models.*;
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.thoughtworks.gocd.analytics.MaterialRevisionMother.materialRevisionFrom;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BuilderTest {
    final DateTimeFormatter format = DateTimeFormatter.ofPattern(DefaultZonedDateTimeTypeAdapter.DATE_PATTERN);

    @Test
    public void shouldBuildAlistOfJobs() throws Exception {
        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(stageJson());

        List<Job> jobs = new Builder().buildJobs(stageStatusRequest.pipeline);
        assertThat(jobs.size(), is(2));
        Job job1 = jobFrom("pipeline-name", 1, "stage-name",
                1, "job1", "Passed", zonedDateTime("2011-07-13T19:43:37.100+0000"),
                zonedDateTime("2011-07-13T19:43:39.100+0000"), zonedDateTime("2011-07-13T19:43:38.100+0000"),
                1, 1, 2, "agent-uuid");

        assertThat(jobs.get(0), is(job1));

        Job job2 = jobFrom("pipeline-name", 1, "stage-name",
                1, "job2", "Passed", zonedDateTime("2011-07-13T19:43:37.100+0000"),
                zonedDateTime("2011-07-13T19:43:57.100+0000"), zonedDateTime("2011-07-13T19:43:47.100+0000"),
                10, 10, 20, "agent-uuid");

        assertThat(jobs.get(1), is(job2));
    }

    @Test
    public void shouldBuildStage() throws Exception {
        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(stageJson());
        Stage stage = new Builder().buildStage(stageStatusRequest.pipeline);

        Stage expectedStage = stageFrom("pipeline-name", 1, "stage-name",
                1, "Unknown", "Passed", "success",
                "changes", zonedDateTime("2011-07-13T19:43:37.100+0000"),

                zonedDateTime("2011-07-13T19:44:37.100+0000"), 20, 10);

        assertThat(expectedStage, is(stage));
    }

    @Test
    public void buildJobs_ShouldHandleUnDefinedAssignTime() throws Exception {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Unknown\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:44:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job2\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:57.100+0000\",\n" +
                "          \"assign-time\": \"\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"agent-uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(json);

        List<Job> jobs = new Builder().buildJobs(stageStatusRequest.pipeline);

        Job job = jobFrom("pipeline-name", 1, "stage-name",
                1, "job2", "Passed", zonedDateTime("2011-07-13T19:43:37.100+0000"),
                zonedDateTime("2011-07-13T19:43:57.100+0000"), null,
                20, 0, 20, "agent-uuid");

        assertThat(jobs.get(0), is(job));
    }

    @Test
    public void shouldBuildPipelineInstance() {
        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(stageJson());
        PipelineInstance pipelineInstance = new Builder().buildPipelineInstance(stageStatusRequest.pipeline);

        PipelineInstance expectedInstance = pipelineInstanceFrom("pipeline-name", 1, "Passed", 20, 10, zonedDateTime("2011-07-13T19:44:37.100+0000"), zonedDateTime("2011-07-13T19:43:37.100+0000"));

        assertThat(expectedInstance, is(pipelineInstance));
    }

    @Test
    public void shouldBuildMaterialRevisionsFromBuildCauseWhichTriggeredTheBuild() {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"git-configuration\": {\n" +
                "            \"shallow-clone\": false,\n" +
                "            \"branch\": \"branch\",\n" +
                "            \"url\": \"http://user:******@gitrepo.com\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"git\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"git-configuration\": {\n" +
                "            \"shallow-clone\": false,\n" +
                "            \"branch\": \"branch\",\n" +
                "            \"url\": \"http://user:******@other_gitrepo.com\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"git\"\n" +
                "        },\n" +
                "        \"changed\": false,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"2\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"pipeline-configuration\": {\n" +
                "            \"pipeline-name\": \"pipeline-name\",\n" +
                "            \"stage-name\": \"stage-name\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"pipeline\"\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"pipeline-name/1/stage-name/1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"state\": \"Completed\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest stageStatusRequest = StageStatusRequest.fromJSON(json);
        List<MaterialRevision> materialRevisions = new Builder().materialRevisionsResponsibleForTheBuild(stageStatusRequest.pipeline);

        assertThat(materialRevisions.size(), is(2));
        assertThat(materialRevisions.get(0), is(materialRevisionFrom(-1, "material_fingerprint",
                "1", "git", stageStatusRequest.pipeline.stage.getCreateTime())));
        assertThat(materialRevisions.get(1), is(materialRevisionFrom(-1, "material_fingerprint",
                "pipeline-name/1/stage-name/1", "pipeline", stageStatusRequest.pipeline.stage.getCreateTime())));
    }

    private ZonedDateTime zonedDateTime(String dateTime) {
        return ZonedDateTime.parse(dateTime, format);
    }

    private PipelineInstance pipelineInstanceFrom(String name, int counter, String result, int totalTime, int timeWaiting, ZonedDateTime lastTransitionTime, ZonedDateTime createdAt) {
        PipelineInstance pipelineInstance = new PipelineInstance();
        pipelineInstance.setName(name);
        pipelineInstance.setCounter(counter);
        pipelineInstance.setResult(result);
        pipelineInstance.setTimeWaitingSecs(timeWaiting);
        pipelineInstance.setTotalTimeSecs(totalTime);
        pipelineInstance.setLastTransitionTime(lastTransitionTime);
        pipelineInstance.setCreatedAt(createdAt);
        return pipelineInstance;
    }

    private Stage stageFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                            String result, String state, String approvalType, String approvedBy,
                            ZonedDateTime scheduledAt, ZonedDateTime completedAt, int duration, int timeWaiting) {
        Stage stage = new Stage();
        stage.setPipelineName(pipelineName);
        stage.setPipelineCounter(pipelineCounter);
        stage.setStageName(stageName);
        stage.setStageCounter(stageCounter);
        stage.setResult(result);
        stage.setState(state);
        stage.setApprovedBy(approvedBy);
        stage.setApprovalType(approvalType);
        stage.setScheduledAt(scheduledAt);
        stage.setCompletedAt(completedAt);
        stage.setTotalTimeSecs(duration);
        stage.setTimeWaitingSecs(timeWaiting);

        return stage;
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                        String jobName, String result, ZonedDateTime scheduledAt, ZonedDateTime completedAt,
                        ZonedDateTime assignedAt, int timeWaiting, int timeBuilding, int duration, String agentUuid) {
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
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"previous-stage-name\": \"previous-stage\",\n" +
                "      \"previous-stage-counter\": 1,\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Unknown\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:44:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job1\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:39.100+0000\",\n" +
                "          \"assign-time\": \"2011-07-13T19:43:38.100+0000\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"agent-uuid\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"job2\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:57.100+0000\",\n" +
                "          \"assign-time\": \"2011-07-13T19:43:47.100+0000\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"agent-uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
