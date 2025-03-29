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

public class AgentMetric extends PersistentObject {

    @Expose
    @SerializedName("pipeline_name")
    String pipeline_name;
    @Expose
    @SerializedName("stage_name")
    String stage_name;
    @Expose
    @SerializedName("job_name")
    String job_name;
    @Expose
    @SerializedName("time_waiting_secs")
    int time_waiting_secs;
    @Expose
    @SerializedName("time_building_secs")
    int time_building_secs;
    @Expose
    @SerializedName("agent_uuid")
    private String agent_uuid;
    @Expose
    @SerializedName("agent_host_name")
    private String agent_host_name;

    @Override
    public String toString() {
        return "AgentMetric{" +
            "pipeline_name='" + pipeline_name + '\'' +
            ", stage_name='" + stage_name + '\'' +
            ", job_name='" + job_name + '\'' +
            ", time_waiting_secs=" + time_waiting_secs +
            ", time_building_secs=" + time_building_secs +
            ", agent_uuid='" + agent_uuid + '\'' +
            ", agent_host_name='" + agent_host_name + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AgentMetric that = (AgentMetric) o;
        return time_waiting_secs == that.time_waiting_secs
            && time_building_secs == that.time_building_secs && pipeline_name.equals(
            that.pipeline_name)
            && stage_name.equals(that.stage_name) && job_name.equals(that.job_name)
            && agent_uuid.equals(that.agent_uuid) && agent_host_name.equals(that.agent_host_name);
    }

    @Override
    public int hashCode() {
        int result = pipeline_name.hashCode();
        result = 31 * result + stage_name.hashCode();
        result = 31 * result + job_name.hashCode();
        result = 31 * result + time_waiting_secs;
        result = 31 * result + time_building_secs;
        result = 31 * result + agent_uuid.hashCode();
        result = 31 * result + agent_host_name.hashCode();
        return result;
    }

    public String getPipeline_name() {
        return pipeline_name;
    }

    public void setPipeline_name(String pipeline_name) {
        this.pipeline_name = pipeline_name;
    }

    public String getStage_name() {
        return stage_name;
    }

    public void setStage_name(String stage_name) {
        this.stage_name = stage_name;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public int getTime_waiting_secs() {
        return time_waiting_secs;
    }

    public void setTime_waiting_secs(int time_waiting_secs) {
        this.time_waiting_secs = time_waiting_secs;
    }

    public int getTime_building_secs() {
        return time_building_secs;
    }

    public void setTime_building_secs(int time_building_secs) {
        this.time_building_secs = time_building_secs;
    }

    public String getAgent_uuid() {
        return agent_uuid;
    }

    public void setAgent_uuid(String agent_uuid) {
        this.agent_uuid = agent_uuid;
    }

    public String getAgent_host_name() {
        return agent_host_name;
    }

    public void setAgent_host_name(String agent_host_name) {
        this.agent_host_name = agent_host_name;
    }

    public AgentMetric(String pipeline_name, String stage_name, String job_name,
        int time_waiting_secs,
        int time_building_secs, String agent_uuid, String agent_host_name) {
        this.pipeline_name = pipeline_name;
        this.stage_name = stage_name;
        this.job_name = job_name;
        this.time_waiting_secs = time_waiting_secs;
        this.time_building_secs = time_building_secs;
        this.agent_uuid = agent_uuid;
        this.agent_host_name = agent_host_name;
    }
}
