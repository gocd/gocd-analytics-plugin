/*
 * Copyright 2024 ThoughtWorks, Inc.
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
import java.time.ZonedDateTime;

public class PipelineTimeSummaryDetails {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("counter")
    private int counter;

    @Expose
    @SerializedName("total_time_secs")
    private int totalTimeSecs;

    @Expose
    @SerializedName("time_waiting_secs")
    private int timeWaitingSecs;

    @Expose
    @SerializedName("created_at")
    private ZonedDateTime createdAt;

    @Expose
    @SerializedName("last_transition_time")
    private ZonedDateTime lastTransitionTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(ZonedDateTime lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public PipelineTimeSummaryDetails(String name, int counter, int totalTimeSecs,
        int timeWaitingSecs,
        ZonedDateTime createdAt, ZonedDateTime lastTransitionTime) {
        this.name = name;
        this.counter = counter;
        this.totalTimeSecs = totalTimeSecs;
        this.timeWaitingSecs = timeWaitingSecs;
        this.createdAt = createdAt;
        this.lastTransitionTime = lastTransitionTime;
    }
}
