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
import static java.lang.String.format;

public class Stage extends PersistentObject {
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
    @SerializedName("result")
    private String result;

    @Expose
    @SerializedName("state")
    private String state;

    @Expose
    @SerializedName("approval_type")
    private String approvalType;

    @Expose
    @SerializedName("approved_by")
    private String approvedBy;

    private String previousStageName;

    private int previousStageCounter;

    @Expose
    @SerializedName("scheduled_at")
    private ZonedDateTime scheduledAt;

    @Expose
    @SerializedName("completed_at")
    private ZonedDateTime completedAt;

    @Expose
    @SerializedName("total_time_secs")
    private int totalTimeSecs;

    @Expose
    @SerializedName("time_waiting_secs")
    private int timeWaitingSecs;

    public Stage() {
    }

    public Stage(String stageName, int stageCounter) {
        this.stageName = stageName;
        this.stageCounter = stageCounter;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(String approvalType) {
        this.approvalType = approvalType;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getPreviousStageName() {
        return previousStageName;
    }

    public void setPreviousStageName(String previousStageName) {
        this.previousStageName = previousStageName;
    }

    public int getPreviousStageCounter() {
        return previousStageCounter;
    }

    public void setPreviousStageCounter(int previousStageCounter) {
        this.previousStageCounter = previousStageCounter;
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
        if (completedAt != null) {
            this.completedAt = completedAt.withZoneSameInstant(UTC);
        }
    }

    public int getTotalTimeSecs() {
        return totalTimeSecs;
    }

    public void setTotalTimeSecs(int totalTimeSecs) {
        this.totalTimeSecs = totalTimeSecs;
    }

    public int getTimeWaitingSecs() {
        return timeWaitingSecs;
    }

    public void setTimeWaitingSecs(int timeWaitingSecs) {
        this.timeWaitingSecs = timeWaitingSecs;
    }

    public boolean isRerun() {
        return stageCounter > 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stage)) return false;
        Stage stage = (Stage) o;
        return getPipelineCounter() == stage.getPipelineCounter() &&
                getStageCounter() == stage.getStageCounter() &&
                getTotalTimeSecs() == stage.getTotalTimeSecs() &&
                getTimeWaitingSecs() == stage.getTimeWaitingSecs() &&
                Objects.equals(getPipelineName(), stage.getPipelineName()) &&
                Objects.equals(getStageName(), stage.getStageName()) &&
                Objects.equals(getResult(), stage.getResult()) &&
                Objects.equals(getState(), stage.getState()) &&
                Objects.equals(getApprovalType(), stage.getApprovalType()) &&
                Objects.equals(getApprovedBy(), stage.getApprovedBy()) &&
                Objects.equals(getScheduledAt(), stage.getScheduledAt()) &&
                Objects.equals(getCompletedAt(), stage.getCompletedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPipelineName(), getPipelineCounter(), getStageName(), getStageCounter(), getResult(), getState(), getApprovalType(), getApprovedBy(), getScheduledAt(), getCompletedAt(), getTotalTimeSecs(), getTimeWaitingSecs());
    }

    @Override
    public String toString() {
        return "Stage{" +
                "pipelineName='" + pipelineName + '\'' +
                ", pipelineCounter=" + pipelineCounter +
                ", stageName='" + stageName + '\'' +
                ", stageCounter=" + stageCounter +
                ", result='" + result + '\'' +
                ", state='" + state + '\'' +
                ", approvalType='" + approvalType + '\'' +
                ", approvedBy='" + approvedBy + '\'' +
                ", scheduledAt=" + getScheduledAt() +
                ", completedAt=" + getCompletedAt() +
                ", totalTimeSecs=" + totalTimeSecs +
                ", timeWaitingSecs=" + timeWaitingSecs +
                '}';
    }

    public String toShortString() {
        return format("%s/%s/%s/%s", pipelineName, pipelineCounter, stageName, stageCounter);
    }
}
