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

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.gocd.analytics.models.AgentUtilization;

import java.time.ZonedDateTime;

public class AgentUtilizationMother {
    String uuid = null;
    int idleDuration = 0;
    int buildingDuration = 0;
    int cancelledDuration = 0;
    int lostContactDuration = 0;
    int unknownDuration = 0;
    String lastKnownState = null;
    ZonedDateTime utilizationDate = null;
    ZonedDateTime lastTransitionTime = null;

    public AgentUtilizationMother withUuid(String uuid) {
        this.uuid = uuid;

        return this;
    }

    public AgentUtilizationMother withLastKnownState(String lastKnownState) {
        this.lastKnownState = lastKnownState;

        return this;
    }

    public AgentUtilizationMother withLastTransitionTime(ZonedDateTime transitionTime) {
        this.lastTransitionTime = transitionTime;

        return this;
    }

    public AgentUtilizationMother withUtilizationDate(ZonedDateTime transitionTime) {
        this.utilizationDate = transitionTime;

        return this;
    }

    public AgentUtilizationMother withIdleDuration(int idleDuration) {
        this.idleDuration = idleDuration;

        return this;
    }

    public AgentUtilizationMother withBuildingDuration(int buildingDuration) {
        this.buildingDuration = buildingDuration;

        return this;
    }

    public AgentUtilizationMother withCancelledDuration(int cancelledDuration) {
        this.cancelledDuration = cancelledDuration;

        return this;
    }

    public AgentUtilizationMother withLostContactDuration(int lostContactDuration) {
        this.lostContactDuration = lostContactDuration;

        return this;
    }


    public AgentUtilizationMother withUnknownDuration(int unknownDuration) {
        this.unknownDuration = unknownDuration;

        return this;
    }

    public AgentUtilization create() {
        AgentUtilization agentUtilization = new AgentUtilization();
        agentUtilization.setUuid(this.uuid);
        agentUtilization.setLastKnownState(this.lastKnownState);
        agentUtilization.setLastTransitionTime(this.lastTransitionTime);
        agentUtilization.setUtilizationDate(this.utilizationDate);
        agentUtilization.setIdleDurationSecs(this.idleDuration);
        agentUtilization.setBuildingDurationSecs(this.buildingDuration);
        agentUtilization.setCancelledDurationSecs(this.cancelledDuration);
        agentUtilization.setLostContactDurationSecs(this.lostContactDuration);
        agentUtilization.setUnknownDurationSecs(this.unknownDuration);

        return agentUtilization;
    }
}
