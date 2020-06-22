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

import java.util.List;
import java.util.stream.Collectors;

public class Builder {
    public List<Job> buildJobs(StageStatusRequest.Pipeline pipeline) {
        List<Job> jobs = pipeline.stage.jobs.stream()
                .map(j -> buildJob(pipeline, j))
                .collect(Collectors.toList());

        return jobs;
    }

    private Job buildJob(StageStatusRequest.Pipeline pipeline, StageStatusRequest.Job j) {
        Job job = new Job();

        job.setPipelineName(pipeline.name);
        job.setPipelineCounter(Integer.valueOf(pipeline.counter));
        job.setStageName(pipeline.stage.name);
        job.setStageCounter(Integer.valueOf(pipeline.stage.counter));
        job.setJobName(j.name);
        job.setResult(j.result);
        job.setScheduledAt(j.getScheduleTime());
        job.setCompletedAt(j.getCompleteTime());
        job.setDurationSecs(j.duration());
        job.setAgentUuid(j.agentUuid);
        job.setAssignedAt(j.getAssignTime());
        job.setTimeWaitingSecs(j.timeWaiting());
        job.setTimeBuildingSecs(j.timeBuilding());

        return job;
    }

    public Stage buildStage(StageStatusRequest.Pipeline pipeline) {
        Stage stage = new Stage();

        stage.setPipelineName(pipeline.name);
        stage.setPipelineCounter(Integer.valueOf(pipeline.counter));
        stage.setStageName(pipeline.stage.name);
        stage.setStageCounter(Integer.valueOf(pipeline.stage.counter));
        stage.setState(pipeline.stage.state);
        stage.setResult(pipeline.stage.result);
        stage.setApprovalType(pipeline.stage.approvalType);
        stage.setApprovedBy(pipeline.stage.approvedBy);
        stage.setScheduledAt(pipeline.stage.getCreateTime());
        stage.setCompletedAt(pipeline.stage.getLastTransitionTime());
        stage.setTotalTimeSecs(duration(pipeline));
        stage.setTimeWaitingSecs(timeWaiting(pipeline));
        stage.setPreviousStageName(pipeline.stage.previousStageName);
        stage.setPreviousStageCounter(pipeline.stage.previousStageCounter);

        return stage;
    }

    public PipelineInstance buildPipelineInstance(StageStatusRequest.Pipeline pipeline) {
        PipelineInstance pipelineInstance = new PipelineInstance();

        pipelineInstance.setName(pipeline.name);
        pipelineInstance.setCounter(Integer.valueOf(pipeline.counter));
        pipelineInstance.setResult(pipeline.stage.state);
        pipelineInstance.setCreatedAt(pipeline.stage.getCreateTime());
        pipelineInstance.setTotalTimeSecs(duration(pipeline));
        pipelineInstance.setTimeWaitingSecs(timeWaiting(pipeline));
        pipelineInstance.setLastTransitionTime(pipeline.stage.getLastTransitionTime());
        return pipelineInstance;
    }

    private int duration(StageStatusRequest.Pipeline pipeline) {
        return pipeline.stage.jobs.stream()
                .map(StageStatusRequest.Job::duration)
                .max(Integer::compare)
                .get();
    }

    private int timeWaiting(StageStatusRequest.Pipeline pipeline) {
        return pipeline.stage.jobs.stream()
                .map(StageStatusRequest.Job::timeWaiting)
                .max(Long::compare)
                .get();
    }

    public List<MaterialRevision> materialRevisionsResponsibleForTheBuild(StageStatusRequest.Pipeline pipeline) {
        return pipeline.buildCause.stream()
                .filter(m -> m.changed)
                .map(m -> new MaterialRevision(m.fingerprint(), m.modifications.get(0).revision, m.type(), pipeline.stage.getCreateTime()))
                .collect(Collectors.toList());
    }
}
