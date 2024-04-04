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

public class PipelineTimeSummaryTwo {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("times")
    private long times;

    @Expose
    @SerializedName("sum_total_time_secs")
    private long sumTotalTimeSecs;

    @Expose
    @SerializedName("sum_time_waiting_secs")
    private long sumTimeWaitingSecs;

    public PipelineTimeSummaryTwo(String name, long times, long sumTotalTimeSecs, long sumTimeWaitingSecs) {
        this.name = name;
        this.times = times;
        this.sumTotalTimeSecs = sumTotalTimeSecs;
        this.sumTimeWaitingSecs = sumTimeWaitingSecs;
    }

    public String getName() {
        return name;
    }

    public long getSumTimeWaitingSecs() {
        return sumTimeWaitingSecs;
    }

    public void setSumTimeWaitingSecs(long sumTimeWaitingSecs) {
        this.sumTimeWaitingSecs = sumTimeWaitingSecs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public long getSumTotalTimeSecs() {
        return sumTotalTimeSecs;
    }

    public void setSumTotalTimeSecs(long sumTotalTimeSecs) {
        this.sumTotalTimeSecs = sumTotalTimeSecs;
    }
}
