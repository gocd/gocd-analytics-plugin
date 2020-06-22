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

package com.thoughtworks.gocd.analytics;


import com.thoughtworks.gocd.analytics.models.Stage;

import java.time.ZonedDateTime;

public class StageMother {
    public static Stage stageWith(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                                  String result, String state, int duration, ZonedDateTime scheduledAt) {
        Stage stage = new Stage();
        stage.setPipelineName(pipelineName);
        stage.setPipelineCounter(pipelineCounter);
        stage.setStageName(stageName);
        stage.setStageCounter(stageCounter);
        stage.setResult(result);
        stage.setState(state);
        stage.setTotalTimeSecs(duration);
        stage.setScheduledAt(scheduledAt);

        return stage;
    }

    public static Stage stageWith(String pipelineName, int pipelineCounter, String stageName, int stageCounter, String result,
                                  String state, int duration, ZonedDateTime scheduledAt, String approvalType, String approvedBy,
                                  ZonedDateTime completedAt, int timeWaiting) {
        Stage stage = stageWith(pipelineName, pipelineCounter, stageName, stageCounter, result, state, duration, scheduledAt);

        stage.setApprovalType(approvalType);
        stage.setApprovedBy(approvedBy);
        stage.setCompletedAt(completedAt);
        stage.setTimeWaitingSecs(timeWaiting);

        return stage;
    }
}
