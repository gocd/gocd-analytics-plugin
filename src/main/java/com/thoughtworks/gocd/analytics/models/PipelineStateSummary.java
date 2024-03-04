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

public class PipelineStateSummary {

    @Expose
    @SerializedName("id")
    private long id;

    @Expose
    @SerializedName("result")
    private String result;

    @Expose
    @SerializedName("total_time_secs")
    private int totalTimeSecs;

    @Expose
    @SerializedName("time_waiting_secs")
    private int timeWaitingSecs;

    @Override
    public String toString() {
        return "PipelineStateSummary{" +
            "id=" + id +
            ", result='" + result + '\'' +
            ", totalTimeSecs=" + totalTimeSecs +
            ", timeWaitingSecs=" + timeWaitingSecs +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PipelineStateSummary that = (PipelineStateSummary) o;

        if (id != that.id) {
            return false;
        }
        if (totalTimeSecs != that.totalTimeSecs) {
            return false;
        }
        if (timeWaitingSecs != that.timeWaitingSecs) {
            return false;
        }
        return result.equals(that.result);
    }

    @Override
    public int hashCode() {
        int result1 = (int) (id ^ (id >>> 32));
        result1 = 31 * result1 + result.hashCode();
        result1 = 31 * result1 + totalTimeSecs;
        result1 = 31 * result1 + timeWaitingSecs;
        return result1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public PipelineStateSummary(long id, String result, int totalTimeSecs, int timeWaitingSecs) {
        this.id = id;
        this.result = result;
        this.totalTimeSecs = totalTimeSecs;
        this.timeWaitingSecs = timeWaitingSecs;
    }
}
