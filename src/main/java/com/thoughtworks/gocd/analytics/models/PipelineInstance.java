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

public class PipelineInstance extends PersistentObject {
    @Expose
    @SerializedName("counter")
    private int counter;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("total_time_secs")
    private int totalTimeSecs;

    @Expose
    @SerializedName("time_waiting_secs")
    private int timeWaitingSecs;

    @Expose
    @SerializedName("scheduled_at")
    private ZonedDateTime createdAt;

    @Expose
    @SerializedName("last_transition_time")
    private ZonedDateTime lastTransitionTime;

    @Expose
    @SerializedName("result")
    private String result;

    @Expose
    @SerializedName("workflow_id")
    private Long workflowId;

    public PipelineInstance() {
    }

    public PipelineInstance(int counter, String name, int totalTimeSecs, int timeWaitingSecs, String result,
                            ZonedDateTime createdAt, ZonedDateTime lastTransitionTime) {
        this.counter = counter;
        this.name = name;
        this.totalTimeSecs = totalTimeSecs;
        this.timeWaitingSecs = timeWaitingSecs;
        this.result = result;
        this.createdAt = createdAt;
        this.lastTransitionTime = lastTransitionTime;
    }

    public PipelineInstance(String name, int counter) {
        this.name = name;
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalTimeSecs() {
        return totalTimeSecs;
    }

    public void setTotalTimeSecs(int totalTimeSecs) {
        this.totalTimeSecs = totalTimeSecs;
    }

    public boolean isFailed() {
        return result.equals("Failed");
    }

    public boolean isPassed() {
        return result.equals("Passed");
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTimeWaitingSecs() {
        return timeWaitingSecs;
    }

    public void setTimeWaitingSecs(int timeWaitingSecs) {
        this.timeWaitingSecs = timeWaitingSecs;
    }

    public ZonedDateTime getLastTransitionTime() {
        if (lastTransitionTime != null) {
            return lastTransitionTime.withZoneSameInstant(UTC);
        }

        return null;
    }

    public void setLastTransitionTime(ZonedDateTime lastTransitionTime) {
        if (lastTransitionTime != null) {
            this.lastTransitionTime = lastTransitionTime.withZoneSameInstant(UTC);
        }
    }

    public ZonedDateTime getCreatedAt() {
        if (createdAt != null) {
            return createdAt.withZoneSameInstant(UTC);
        }

        return null;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt.withZoneSameInstant(UTC);
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public String toString() {
        return "PipelineInstance{" +
                "id=" + id +
                ", counter=" + counter +
                ", name='" + name + '\'' +
                ", totalTimeSecs=" + totalTimeSecs +
                ", timeWaitingSecs=" + timeWaitingSecs +
                ", createdAt=" + getCreatedAt() +
                ", lastTransitionTime=" + getLastTransitionTime() +
                ", result='" + result + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineInstance that = (PipelineInstance) o;
        return counter == that.counter &&
                totalTimeSecs == that.totalTimeSecs &&
                timeWaitingSecs == that.timeWaitingSecs &&
                Objects.equals(name, that.name) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(lastTransitionTime, that.lastTransitionTime) &&
                Objects.equals(result, that.result) &&
                Objects.equals(workflowId, that.workflowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counter, name, totalTimeSecs, timeWaitingSecs, createdAt, lastTransitionTime, result, workflowId);
    }
}
