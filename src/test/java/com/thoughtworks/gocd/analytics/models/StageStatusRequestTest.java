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

package com.thoughtworks.gocd.analytics.models;

import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StageStatusRequestTest {

    @Test
    public void shouldDeserializeFromJSONWithoutLoosingAnyData() throws Exception {
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
                "          \"mercurial-configuration\": {\n" +
                "            \"url\": \"http://user:******@hgrepo.com\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"mercurial\"\n" +
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
                "          \"svn-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"check-externals\": false,\n" +
                "            \"url\": \"http://user:******@svnrepo.com\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"svn\"\n" +
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
                "          \"tfs-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"project-path\": \"project-path\",\n" +
                "            \"domain\": \"domain\",\n" +
                "            \"url\": \"http://user:******@tfsrepo.com\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"tfs\"\n" +
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
                "          \"perforce-configuration\": {\n" +
                "            \"username\": \"username\",\n" +
                "            \"use-tickets\": false,\n" +
                "            \"view\": \"view\",\n" +
                "            \"url\": \"127.0.0.1:1666\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"perforce\"\n" +
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
                "      },\n" +
                "      {\n" +
                "        \"material\": {\n" +
                "          \"plugin-id\": \"pluginid\",\n" +
                "          \"package-configuration\": {\n" +
                "            \"k3\": \"package-v1\"\n" +
                "          },\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"package\",\n" +
                "          \"repository-configuration\": {\n" +
                "            \"k1\": \"repo-v1\"\n" +
                "          }\n" +
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
                "          \"plugin-id\": \"pluginid\",\n" +
                "          \"fingerprint\": \"material_fingerprint\",\n" +
                "          \"type\": \"scm\",\n" +
                "          \"scm-configuration\": {\n" +
                "            \"k1\": \"v1\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"changed\": true,\n" +
                "        \"modifications\": [\n" +
                "          {\n" +
                "            \"revision\": \"1\",\n" +
                "            \"modified-time\": \"2016-04-06T12:50:03.317+0000\",\n" +
                "            \"data\": {}\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"previous-stage-name\": \"previous-stage\",\n" +
                "      \"previous-stage-counter\": 1,\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:39.100+0000\",\n" +
                "          \"assign-time\": \"2011-07-13T19:43:38.100+0000\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);
        String serializedAgain = StageStatusRequest.GSON.toJson(request);
        JSONAssert.assertEquals(json, serializedAgain, true);
        StageStatusRequest.Job job = request.pipeline.stage.jobs.get(0);
        Assert.assertEquals(2, job.duration());
        Assert.assertEquals(1, job.timeWaiting());
        Assert.assertEquals(1, job.timeBuilding());
    }

    @Test
    public void stageShouldBeCompletedIfStateIsPassed_Failed_Cancelled() throws Exception {
        StageStatusRequest.Stage stage = new StageStatusRequest.Stage();
        stage.state = "Passed";

        assertTrue(stage.isCompleted());

        stage.state = "Failed";
        assertTrue(stage.isCompleted());

        stage.state = "Cancelled";
        assertTrue(stage.isCompleted());

        stage.state = "Cancelling";
        assertFalse(stage.isCompleted());
    }

    @Test
    public void stageShouldBeReRunIfCounterIsGreaterThanOne() throws Exception {
        StageStatusRequest.Stage stage = new StageStatusRequest.Stage();
        stage.counter = "1";

        assertFalse(stage.isReRun());

        stage.counter = "2";
        assertTrue(stage.isReRun());

        stage.counter = "3";
        assertTrue(stage.isReRun());
    }

    @Test
    public void jobTimeWaiting_ShouldBeDurationBetweenScheduleTimeAndCompleteTimeIfAgentIsNotAssigned() throws Exception {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:47.100+0000\",\n" +
                "          \"assign-time\": \"\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);
        StageStatusRequest.Job job = request.pipeline.stage.jobs.get(0);
        Assert.assertEquals(10, job.duration());
        Assert.assertEquals(10, job.timeWaiting());
    }

    @Test
    public void jobTimeBuilding_ShouldBe0IfAgentIsNotAssigned() throws Exception {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"last-transition-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "        {\n" +
                "          \"name\": \"job-name\",\n" +
                "          \"schedule-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "          \"complete-time\": \"2011-07-13T19:43:47.100+0000\",\n" +
                "          \"assign-time\": \"\",\n" +
                "          \"state\": \"Completed\",\n" +
                "          \"result\": \"Passed\",\n" +
                "          \"agent-uuid\": \"uuid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);
        StageStatusRequest.Job job = request.pipeline.stage.jobs.get(0);
        Assert.assertEquals(10, job.duration());
        Assert.assertEquals(0, job.timeBuilding());
    }

    @Test
    public void buildCause_shouldExposeMaterialFingerprint() {
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
                "        ]\n" +
                "      },\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);

        assertThat(request.pipeline.buildCause.get(0).fingerprint(), is("material_fingerprint"));
    }

    @Test
    public void stage_shouldExposePreviousStageDetails() {
        String json = "{\n" +
                "  \"pipeline\": {\n" +
                "    \"name\": \"pipeline-name\",\n" +
                "    \"counter\": \"1\",\n" +
                "    \"group\": \"pipeline-group\",\n" +
                "    \"build-cause\": [\n" +
                "    ],\n" +
                "    \"stage\": {\n" +
                "      \"name\": \"stage-name\",\n" +
                "      \"counter\": \"1\",\n" +
                "      \"approval-type\": \"success\",\n" +
                "      \"approved-by\": \"changes\",\n" +
                "      \"previous-stage-name\": \"previous-stage\",\n" +
                "      \"previous-stage-counter\": 1,\n" +
                "      \"state\": \"Passed\",\n" +
                "      \"result\": \"Passed\",\n" +
                "      \"create-time\": \"2011-07-13T19:43:37.100+0000\",\n" +
                "      \"jobs\": [\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        StageStatusRequest request = StageStatusRequest.fromJSON(json);

        assertThat(request.pipeline.stage.previousStageName, is("previous-stage"));
        assertThat(request.pipeline.stage.previousStageCounter, is(1));
    }
}
