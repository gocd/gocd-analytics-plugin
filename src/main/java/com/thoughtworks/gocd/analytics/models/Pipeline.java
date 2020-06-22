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

import java.util.List;
import java.util.Objects;

public class Pipeline extends PersistentObject {
    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("avg_wait_time_secs")
    private Integer avgWaitTimeSecs;

    @Expose
    @SerializedName("avg_build_time_secs")
    private Integer avgBuildTimeSecs;

    @Expose
    @SerializedName("instances")
    private List<PipelineInstance> instances;

    public Pipeline() {
    }

    public Pipeline(String name, List<PipelineInstance> instances) {
        this.name = name;
        this.instances = instances;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PipelineInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<PipelineInstance> instances) {
        this.instances = instances;
    }

    public Integer getAvgWaitTimeSecs() {
        return avgWaitTimeSecs;
    }

    public void setAvgWaitTimeSecs(int avgWaitTimeSecs) {
        this.avgWaitTimeSecs = avgWaitTimeSecs;
    }

    public Integer getAvgBuildTimeSecs() {
        return avgBuildTimeSecs;
    }

    public void setAvgBuildTimeSecs(int avgBuildTimeSecs) {
        this.avgBuildTimeSecs = avgBuildTimeSecs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pipeline)) return false;
        Pipeline pipeline = (Pipeline) o;
        return Objects.equals(getName(), pipeline.getName()) &&
                Objects.equals(getAvgWaitTimeSecs(), pipeline.getAvgWaitTimeSecs()) &&
                Objects.equals(getAvgBuildTimeSecs(), pipeline.getAvgBuildTimeSecs()) &&
                Objects.equals(getInstances(), pipeline.getInstances());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAvgWaitTimeSecs(), getAvgBuildTimeSecs(), getInstances());
    }
}
