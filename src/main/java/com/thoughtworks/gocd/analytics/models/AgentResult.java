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

public class AgentResult extends PersistentObject {

    @Expose
    @SerializedName("host_name")
    String host_name;
    @Expose
    @SerializedName("agent_uuid")
    private String agent_uuid;
    @Expose
    @SerializedName("result")
    private String result;

    public AgentResult(String host_name, String agentUuid, String result) {
        this.host_name = host_name;
        this.agent_uuid = agentUuid;
        this.result = result;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    public String getAgentUuid() {
        return agent_uuid;
    }

    public void setUuid(String agentUuid) {
        this.agent_uuid = agentUuid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AgentResult{" +
            "agentHostName='" + host_name + '\'' +
            ", agentUuid='" + agent_uuid + '\'' +
            ", result='" + result + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AgentResult that = (AgentResult) o;
        return host_name.equals(that.host_name) && agent_uuid.equals(that.host_name) && result.equals(
            that.result);
    }

    @Override
    public int hashCode() {
        int result1 = host_name.hashCode();
        result1 = 31 * result1 + agent_uuid.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }
}
