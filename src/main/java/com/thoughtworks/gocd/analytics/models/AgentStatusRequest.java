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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;

import java.time.ZonedDateTime;

public class AgentStatusRequest {
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new DefaultZonedDateTimeTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation().
                    create();

    @Expose
    @SerializedName("uuid")
    private String uuid;

    @Expose
    @SerializedName("host_name")
    private String hostName;

    @Expose
    @SerializedName("is_elastic")
    private boolean isElastic;

    @Expose
    @SerializedName("ip_address")
    private String ipAddress;

    @Expose
    @SerializedName("operating_system")
    private String operatingSystem;

    @Expose
    @SerializedName("free_space")
    private String freeSpace;

    @Expose
    @SerializedName("agent_config_state")
    private String agentConfigState;

    @Expose
    @SerializedName("agent_state")
    private String agentState;

    @Expose
    @SerializedName("build_state")
    private String buildState;

    @Expose
    @SerializedName("transition_time")
    private ZonedDateTime transitionTime;

    public static AgentStatusRequest fromJSON(String json) {
        return GSON.fromJson(json, AgentStatusRequest.class);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isElastic() {
        return isElastic;
    }

    public void setIsElastic(boolean isElastic) {
        this.isElastic = isElastic;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(String freeSpace) {
        this.freeSpace = freeSpace;
    }

    public String getAgentConfigState() {
        return agentConfigState;
    }

    public void setAgentConfigState(String agentConfigState) {
        this.agentConfigState = agentConfigState;
    }

    public String getAgentState() {
        return agentState;
    }

    public void setAgentState(String agentState) {
        this.agentState = agentState;
    }

    public String getBuildState() {
        return buildState;
    }

    public void setBuildState(String buildState) {
        this.buildState = buildState;
    }

    public ZonedDateTime getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(ZonedDateTime transitionTime) {
        this.transitionTime = transitionTime;
    }

    @Override
    public String toString() {
        return "AgentStatusRequest{" +
                "uuid='" + uuid + '\'' +
                ", hostName='" + hostName + '\'' +
                ", isElastic=" + isElastic +
                ", ipAddress='" + ipAddress + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", freeSpace='" + freeSpace + '\'' +
                ", agentConfigState='" + agentConfigState + '\'' +
                ", agentState='" + agentState + '\'' +
                ", buildState='" + buildState + '\'' +
                ", transitionTime=" + getTransitionTime() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatusRequest that = (AgentStatusRequest) o;

        if (isElastic != that.isElastic) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;
        if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) return false;
        if (operatingSystem != null ? !operatingSystem.equals(that.operatingSystem) : that.operatingSystem != null)
            return false;
        if (freeSpace != null ? !freeSpace.equals(that.freeSpace) : that.freeSpace != null) return false;
        if (agentConfigState != null ? !agentConfigState.equals(that.agentConfigState) : that.agentConfigState != null)
            return false;
        if (agentState != null ? !agentState.equals(that.agentState) : that.agentState != null) return false;
        if (buildState != null ? !buildState.equals(that.buildState) : that.buildState != null) return false;
        return transitionTime != null ? transitionTime.equals(that.transitionTime) : that.transitionTime == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (isElastic ? 1 : 0);
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (operatingSystem != null ? operatingSystem.hashCode() : 0);
        result = 31 * result + (freeSpace != null ? freeSpace.hashCode() : 0);
        result = 31 * result + (agentConfigState != null ? agentConfigState.hashCode() : 0);
        result = 31 * result + (agentState != null ? agentState.hashCode() : 0);
        result = 31 * result + (buildState != null ? buildState.hashCode() : 0);
        result = 31 * result + (transitionTime != null ? transitionTime.hashCode() : 0);
        return result;
    }
}
