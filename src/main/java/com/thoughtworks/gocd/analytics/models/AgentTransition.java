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

import java.time.ZonedDateTime;

public class AgentTransition {
    @Expose
    @SerializedName("uuid")
    private String uuid;

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

    public static AgentTransition fromRequest(AgentStatusRequest request) {
        AgentTransition agentTransition = new AgentTransition();

        agentTransition.setUuid(request.getUuid());
        agentTransition.setAgentConfigState(request.getAgentConfigState());
        agentTransition.setAgentState(request.getAgentState());
        agentTransition.setBuildState(request.getBuildState());
        agentTransition.setTransitionTime(request.getTransitionTime());

        return agentTransition;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentTransition that = (AgentTransition) o;

        if (!uuid.equals(that.uuid)) return false;
        if (!agentConfigState.equals(that.agentConfigState)) return false;
        if (!agentState.equals(that.agentState)) return false;
        if (!buildState.equals(that.buildState)) return false;
        return transitionTime.equals(that.transitionTime);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + agentConfigState.hashCode();
        result = 31 * result + agentState.hashCode();
        result = 31 * result + buildState.hashCode();
        result = 31 * result + transitionTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AgentTransition{" +
                "uuid='" + uuid + '\'' +
                ", agentConfigState='" + agentConfigState + '\'' +
                ", agentState='" + agentState + '\'' +
                ", buildState='" + buildState + '\'' +
                ", transitionTime=" + transitionTime +
                '}';
    }
}
