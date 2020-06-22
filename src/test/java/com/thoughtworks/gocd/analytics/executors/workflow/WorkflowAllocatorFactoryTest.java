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

import com.thoughtworks.gocd.analytics.models.Stage;
import org.junit.Test;

import java.time.ZonedDateTime;

import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static org.junit.Assert.assertTrue;

public class WorkflowAllocatorFactoryTest {

    @Test
    public void automaticTriggerOfFirstStageOfPipeline_shouldBeHandledBy_WorkflowAllocatorForFirstStageOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForFirstStageOfPipeline));
    }

    @Test
    public void automaticTriggerOfSubsequentStagesOfPipeline_shouldBeHandledBy_WorkflowAllocatorForSubsequentStagesOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", "stage-1", 1, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForSubsequentStagesOfPipeline));
    }

    @Test
    public void automaticTriggerOfSubsequentStagesOfPipelineOnRerunOfPreviousStage_shouldBeHandledBy_WorkflowAllocatorForSubsequentStagesOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-2", 2, "success",
                "changes", "stage-1", 2, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForSubsequentStagesOfPipeline));
    }

    @Test
    public void manualTriggerOfAutoSchedulablePipeline_shouldBeHandledBy_WorkflowAllocatorForManualTriggerOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-1", 1, "success",
                "admin", null, 0, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForManualTriggerOfPipeline));
    }

    @Test
    public void manualTriggerOfPipelineWithAutoSchedulingTurnedOff_shouldBeHandledBy_WorkflowAllocatorForManualTriggerOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-1", 1, "manual",
                "admin", null, 0, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForManualTriggerOfPipeline));
    }

    @Test
    public void manualTriggerOfSubsequentManualStagesInAPipeline_shouldBeHandledBy_WorkflowAllocatorForSubsequentStagesOfPipeline() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-2", 1, "manual",
                "admin", "stage-1", 1, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForSubsequentStagesOfPipeline));
    }

    @Test
    public void reRunOfFirstStageInAPipeline_shouldBeHandledBy_WorkflowAllocatorForStageReRuns() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-1", 2, "success",
                "admin", null, 0, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForStageReRuns));
    }

    @Test
    public void reRunOfSubsequentStagesInAPipeline_shouldBeHandledBy_WorkflowAllocatorForStageReRuns() {
        Stage stage = stageFrom(1, "pipeline-name", 1, "stage-2", 2, "success",
                "admin", null, 1, ZonedDateTime.now());

        WorkflowAllocator workflowAllocator = new WorkflowAllocatorFactory().allocatorFor(stage);

        assertTrue((workflowAllocator instanceof WorkflowAllocatorForStageReRuns));
    }
}
