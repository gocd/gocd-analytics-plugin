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

public class Agent extends PersistentObject {
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
    @SerializedName("config_state")
    private String configState;

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

    public String getConfigState() {
        return configState;
    }

    public void setConfigState(String configState) {
        this.configState = configState.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agent agent = (Agent) o;

        if (id != agent.id) return false;
        if (isElastic != agent.isElastic) return false;
        if (uuid != null ? !uuid.equals(agent.uuid) : agent.uuid != null) return false;
        if (hostName != null ? !hostName.equals(agent.hostName) : agent.hostName != null) return false;
        if (ipAddress != null ? !ipAddress.equals(agent.ipAddress) : agent.ipAddress != null) return false;
        if (operatingSystem != null ? !operatingSystem.equals(agent.operatingSystem) : agent.operatingSystem != null)
            return false;
        if (freeSpace != null ? !freeSpace.equals(agent.freeSpace) : agent.freeSpace != null) return false;
        return configState != null ? configState.equals(agent.configState) : agent.configState == null;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (isElastic ? 1 : 0);
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (operatingSystem != null ? operatingSystem.hashCode() : 0);
        result = 31 * result + (freeSpace != null ? freeSpace.hashCode() : 0);
        result = 31 * result + (configState != null ? configState.hashCode() : 0);
        return result;
    }
}
