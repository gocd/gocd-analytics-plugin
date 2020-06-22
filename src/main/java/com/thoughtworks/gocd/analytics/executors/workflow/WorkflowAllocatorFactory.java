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

package com.thoughtworks.gocd.analytics.executors.workflow;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.commons.lang3.StringUtils;

import static java.lang.String.format;

public class WorkflowAllocatorFactory {
    private static final Logger LOG = Logger.getLoggerFor(WorkflowAllocatorFactory.class);
    private final String MANUAL_APPROVAL_TYPE = "manual";

    private final String AUTOMATIC_TRIGGER = "changes";
    private final String TIMER_TRIGGER = "timer";


    public WorkflowAllocator allocatorFor(Stage stage) {
        if (isStageRerun(stage)) {
            LOG.debug("[Workflow-Allocator] Stage is a re-run - using WorkflowAllocatorForStageReRuns");
            return new WorkflowAllocatorForStageReRuns();
        }

        if(isManualTriggerOfPipeline(stage)) {
            LOG.debug("[Workflow-Allocator] Manual trigger of pipeline - using WorkflowAllocatorForManualTriggerOfPipeline");
            return new WorkflowAllocatorForManualTriggerOfPipeline();
        }

        if (isManualStage(stage)) {
            LOG.debug("[Workflow-Allocator] Manual trigger of stage - using WorkflowAllocatorForSubsequentStagesOfPipeline");
            return new WorkflowAllocatorForSubsequentStagesOfPipeline();
        }

        if (!hasPreviousStage(stage)) {
            LOG.debug("[Workflow-Allocator] Automatic run of first stage of pipeline - using WorkflowAllocatorForFirstStageOfPipeline");
            return new WorkflowAllocatorForFirstStageOfPipeline();
        }

        if (hasPreviousStage(stage) && !isStageRerun(stage)) {
            LOG.debug("[Workflow-Allocator] Automatic run of subsequent stage of pipelines - using WorkflowAllocatorForSubsequentStagesOfPipeline");
            return new WorkflowAllocatorForSubsequentStagesOfPipeline();
        }

        throw new RuntimeException(format("Unable to identify Workflow Allocator for the stage: %s", stage));
    }

    private boolean hasPreviousStage(Stage stage) {
        return StringUtils.isNotBlank(stage.getPreviousStageName());
    }

    private boolean isManualStage(Stage stage) {
        return MANUAL_APPROVAL_TYPE.equalsIgnoreCase(stage.getApprovalType()) && hasPreviousStage(stage);
    }

    private boolean isStageRerun(Stage stage) {
        return stage.isRerun() && !hasPreviousStage(stage);
    }

    private boolean isManualTriggerOfPipeline(Stage stage) {
        return (!isAutoTriggered(stage)
                && !isTimerTriggered(stage)
                && !hasPreviousStage(stage)
                && !isStageRerun(stage));
    }

    private boolean isAutoTriggered(Stage stage) {
        return AUTOMATIC_TRIGGER.equalsIgnoreCase(stage.getApprovedBy());
    }

    private boolean isTimerTriggered(Stage stage) {
        return TIMER_TRIGGER.equalsIgnoreCase(stage.getApprovedBy());
    }
}
