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


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.analytics.db.PersistentObject;

import java.time.ZonedDateTime;
import java.util.Objects;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;

public class Job extends PersistentObject {
    @Expose
    @SerializedName("pipeline_name")
    private String pipelineName;

    @Expose
    @SerializedName("pipeline_counter")
    private int pipelineCounter;

    @Expose
    @SerializedName("stage_name")
    private String stageName;

    @Expose
    @SerializedName("stage_counter")
    private int stageCounter;

    @Expose
    @SerializedName("job_name")
    private String jobName;

    @Expose
    @SerializedName("result")
    private String result;

    @Expose
    @SerializedName("scheduled_at")
    private ZonedDateTime scheduledAt;

    @Expose
    @SerializedName("completed_at")
    private ZonedDateTime completedAt;

    @Expose
    @SerializedName("assigned_at")
    private ZonedDateTime assignedAt;

    @Expose
    @SerializedName("time_waiting_secs")
    private int timeWaitingSecs;

    @Expose
    @SerializedName("time_building_secs")
    private int timeBuildingSecs;

    @Expose
    @SerializedName("duration_secs")
    private int durationSecs;

    @Expose
    @SerializedName("agent_uuid")
    private String agentUuid;

    public int getTimeBuildingSecs() {
        return timeBuildingSecs;
    }

    public void setTimeBuildingSecs(int timeBuildingSecs) {
        this.timeBuildingSecs = timeBuildingSecs;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public int getPipelineCounter() {
        return pipelineCounter;
    }

    public void setPipelineCounter(int pipelineCounter) {
        this.pipelineCounter = pipelineCounter;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public int getStageCounter() {
        return stageCounter;
    }

    public void setStageCounter(int stageCounter) {
        this.stageCounter = stageCounter;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ZonedDateTime getScheduledAt() {
        if (scheduledAt != null) {
            return scheduledAt.withZoneSameInstant(UTC);
        }

        return null;
    }

    public void setScheduledAt(ZonedDateTime scheduledAt) {
        this.scheduledAt = scheduledAt.withZoneSameInstant(UTC);
    }

    public ZonedDateTime getCompletedAt() {
        if (completedAt != null) {
            return completedAt.withZoneSameInstant(UTC);
        }

        return null;
    }

    public void setCompletedAt(ZonedDateTime completedAt) {
        this.completedAt = completedAt.withZoneSameInstant(UTC);
    }

    public ZonedDateTime getAssignedAt() {
        if (assignedAt != null) {
            return assignedAt.withZoneSameInstant(UTC);
        }

        return null;
    }

    public void setAssignedAt(ZonedDateTime assignedAt) {
        if (assignedAt != null) {
            this.assignedAt = assignedAt.withZoneSameInstant(UTC);
        }
    }

    public int getTimeWaitingSecs() {
        return timeWaitingSecs;
    }

    public void setTimeWaitingSecs(int timeWaitingSecs) {
        this.timeWaitingSecs = timeWaitingSecs;
    }

    public int getDurationSecs() {
        return durationSecs;
    }

    public void setDurationSecs(int durationSecs) {
        this.durationSecs = durationSecs;
    }

    public String getAgentUuid() {
        return agentUuid;
    }

    public void setAgentUuid(String agentUuid) {
        this.agentUuid = agentUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Job)) return false;
        Job job = (Job) o;
        return getPipelineCounter() == job.getPipelineCounter() &&
                getStageCounter() == job.getStageCounter() &&
                getTimeWaitingSecs() == job.getTimeWaitingSecs() &&
                getTimeBuildingSecs() == job.getTimeBuildingSecs() &&
                getDurationSecs() == job.getDurationSecs() &&
                Objects.equals(getPipelineName(), job.getPipelineName()) &&
                Objects.equals(getStageName(), job.getStageName()) &&
                Objects.equals(getJobName(), job.getJobName()) &&
                Objects.equals(getResult(), job.getResult()) &&
                Objects.equals(getScheduledAt(), job.getScheduledAt()) &&
                Objects.equals(getCompletedAt(), job.getCompletedAt()) &&
                Objects.equals(getAssignedAt(), job.getAssignedAt()) &&
                Objects.equals(getAgentUuid(), job.getAgentUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPipelineName(), getPipelineCounter(), getStageName(), getStageCounter(), getJobName(), getResult(), getScheduledAt(), getCompletedAt(), getAssignedAt(), getTimeWaitingSecs(), getTimeBuildingSecs(), getDurationSecs(), getAgentUuid());
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", pipelineName='" + pipelineName + '\'' +
                ", pipelineCounter=" + pipelineCounter +
                ", stageName='" + stageName + '\'' +
                ", stageCounter=" + stageCounter +
                ", jobName='" + jobName + '\'' +
                ", result='" + result + '\'' +
                ", scheduledAt=" + getScheduledAt() +
                ", completedAt=" + getCompletedAt() +
                ", assignedAt=" + getAssignedAt() +
                ", timeWaitingSecs=" + timeWaitingSecs +
                ", timeBuildingSecs=" + timeBuildingSecs +
                ", durationSecs=" + durationSecs +
                ", agentUuid='" + agentUuid + '\'' +
                '}';
    }
}
