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
    public static AgentUtilization agentUtilizationWith(String uuid, ZonedDateTime utilizationDate, String lastKnownState,
                                                        ZonedDateTime transitionTime, int idleDuration, int buildingDuration,
                                                        int cancelledDuration, String hostName) {
        AgentUtilization agentUtilization = new AgentUtilization();

        agentUtilization.setUuid(uuid);
        agentUtilization.setUtilizationDate(utilizationDate);
        agentUtilization.setLastKnownState(lastKnownState);
        agentUtilization.setLastTransitionTime(transitionTime);
        agentUtilization.setIdleDurationSecs(idleDuration);
        agentUtilization.setBuildingDurationSecs(buildingDuration);
        agentUtilization.setCancelledDurationSecs(cancelledDuration);
        agentUtilization.setAgentHostName(hostName);

        return agentUtilization;
    }
}
