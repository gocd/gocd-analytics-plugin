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
import com.thoughtworks.gocd.analytics.exceptions.UnrecognizedAgentStateException;
import com.thoughtworks.gocd.analytics.utils.DateUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentUtilization extends PersistentObject {
    private static final int FULL_DAY_IN_SECONDS = 86400;
    private static Map<String, DurationUpdater> durationUpdaters = new HashMap<String, DurationUpdater>();

    static {
        durationUpdaters.put("idle", new IdleDurationUpdater());
        durationUpdaters.put("building", new BuildingDurationUpdater());
        durationUpdaters.put("cancelled", new CancelledDurationUpdater());
        durationUpdaters.put("missing", new LostContanctDurationUpdater());
        durationUpdaters.put("lostcontact", new LostContanctDurationUpdater());
        durationUpdaters.put("unknown", new UnknownDurationUpdater());
    }

    @Expose
    @SerializedName("agent_host_name")
    String agentHostName;
    @Expose
    @SerializedName("uuid")
    private String uuid;
    @Expose
    @SerializedName("idle_duration_secs")
    private int idleDurationSecs;
    @Expose
    @SerializedName("building_duration_secs")
    private int buildingDurationSecs;
    @Expose
    @SerializedName("cancelled_duration_secs")
    private int cancelledDurationSecs;
    @Expose
    @SerializedName("lost_contact_duration_secs")
    private int lostContactDurationSecs;
    @Expose
    @SerializedName("unknown_duration_secs")
    private int unknownDurationSecs;
    @Expose
    @SerializedName("last_known_state")
    private String lastKnownState;
    @Expose
    @SerializedName("utilization_date")
    private ZonedDateTime utilizationDate;
    @Expose
    @SerializedName("last_transition_time")
    private ZonedDateTime lastTransitionTime;

    public static AgentUtilization create(String agentUuid, String agentState, ZonedDateTime transitionTime) {
        AgentUtilization agentUtilization = new AgentUtilization();

        agentUtilization.setUuid(agentUuid);
        agentUtilization.setLastKnownState(agentState);
        agentUtilization.setUtilizationDate(transitionTime);
        agentUtilization.setLastTransitionTime(transitionTime);

        return agentUtilization;
    }

    public static List<AgentUtilization> createUtilizationsPostLastKnownUtilization(AgentUtilization lastKnownUtilization,
                                                                                    String uuid, String agentState,
                                                                                    ZonedDateTime currentTransitionTime) {
        ZonedDateTime start = lastKnownUtilization.getUtilizationDate().plusDays(1);

        List<AgentUtilization> agentUtilizations = Stream.iterate(start, date -> date.plusDays(1))
                .limit(Duration.between(start.truncatedTo(ChronoUnit.DAYS), currentTransitionTime.truncatedTo(ChronoUnit.DAYS)).toDays())
                .map(date -> AgentUtilization.createUtilizationForFullDay(lastKnownUtilization, date)).collect(Collectors.toList());

        agentUtilizations.add(AgentUtilization.createUtilizationForToday(uuid, agentState, currentTransitionTime, lastKnownUtilization.getLastKnownState()));

        return agentUtilizations;
    }

    private static AgentUtilization createUtilizationForToday(String uuid, String currentAgentState, ZonedDateTime transitionTime, String lastKnownState) {
        AgentUtilization agentUtilization = new AgentUtilization();

        agentUtilization.setUuid(uuid);
        agentUtilization.setLastKnownState(currentAgentState);
        agentUtilization.setLastTransitionTime(transitionTime);
        agentUtilization.setUtilizationDate(transitionTime);

        updaterFor(lastKnownState).update(agentUtilization, DateUtils.durationFromStartOfDayInSeconds(transitionTime));

        return agentUtilization;
    }

    private static AgentUtilization createUtilizationForFullDay(AgentUtilization lastKnownUtilization, ZonedDateTime date) {
        AgentUtilization agentUtilization = new AgentUtilization();

        agentUtilization.setUuid(lastKnownUtilization.getUuid());
        agentUtilization.setLastKnownState(lastKnownUtilization.getLastKnownState());
        agentUtilization.setUtilizationDate(date);
        agentUtilization.setLastTransitionTime(date);

        updaterFor(agentUtilization.getLastKnownState()).update(agentUtilization, FULL_DAY_IN_SECONDS);

        return agentUtilization;
    }

    private static DurationUpdater updaterFor(String agentState) {
        DurationUpdater durationUpdater = durationUpdaters.get(agentState.toLowerCase());
        if (durationUpdater == null) {
            throw new UnrecognizedAgentStateException(agentState);
        }
        return durationUpdater;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAgentHostName() {
        return agentHostName;
    }

    public void setAgentHostName(String agentHostName) {
        this.agentHostName = agentHostName;
    }

    public ZonedDateTime getUtilizationDate() {
        return utilizationDate != null
                ? utilizationDate.withZoneSameInstant(DateUtils.UTC)
                : null;
    }

    public void setUtilizationDate(ZonedDateTime utilizationDate) {
        if (utilizationDate != null) {
            this.utilizationDate = utilizationDate.withZoneSameInstant(DateUtils.UTC);
        }
    }

    public int getIdleDurationSecs() {
        return idleDurationSecs;
    }

    public void setIdleDurationSecs(int idleDurationSecs) {
        this.idleDurationSecs = idleDurationSecs;
    }

    public int getBuildingDurationSecs() {
        return buildingDurationSecs;
    }

    public void setBuildingDurationSecs(int buildingDurationSecs) {
        this.buildingDurationSecs = buildingDurationSecs;
    }

    public int getCancelledDurationSecs() {
        return cancelledDurationSecs;
    }

    public void setCancelledDurationSecs(int cancelledDurationSecs) {
        this.cancelledDurationSecs = cancelledDurationSecs;
    }

    public int getLostContactDurationSecs() {
        return lostContactDurationSecs;
    }

    public void setLostContactDurationSecs(int lostContactDurationSecs) {
        this.lostContactDurationSecs = lostContactDurationSecs;
    }

    public int getUnknownDurationSecs() {
        return unknownDurationSecs;
    }

    public void setUnknownDurationSecs(int unknownDurationSecs) {
        this.unknownDurationSecs = unknownDurationSecs;
    }

    public String getLastKnownState() {
        return lastKnownState;
    }

    public void setLastKnownState(String lastKnownState) {
        this.lastKnownState = lastKnownState;
    }

    public ZonedDateTime getLastTransitionTime() {
        return lastTransitionTime != null
                ? lastTransitionTime.withZoneSameInstant(DateUtils.UTC)
                : null;
    }

    public void setLastTransitionTime(ZonedDateTime lastTransitionTime) {
        if (lastTransitionTime != null) {
            this.lastTransitionTime = lastTransitionTime.withZoneSameInstant(DateUtils.UTC);
        }
    }

    public AgentUtilization update(String newAgentState, ZonedDateTime currentTransitionTime) {
        updaterFor(this.lastKnownState).update(this,
                DateUtils.durationBetweenInSeconds(this.getLastTransitionTime(), currentTransitionTime));

        this.setLastKnownState(newAgentState);
        this.setLastTransitionTime(currentTransitionTime);

        return this;
    }

    public AgentUtilization updateUtilizationTillEOD() {
        int durationTillMidnight = DateUtils.durationTillEndOfDayInSeconds(this.getLastTransitionTime());

        updaterFor(this.lastKnownState).update(this, durationTillMidnight);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentUtilization)) return false;
        AgentUtilization that = (AgentUtilization) o;
        return getIdleDurationSecs() == that.getIdleDurationSecs() &&
                getBuildingDurationSecs() == that.getBuildingDurationSecs() &&
                getCancelledDurationSecs() == that.getCancelledDurationSecs() &&
                getLostContactDurationSecs() == that.getLostContactDurationSecs() &&
                getUnknownDurationSecs() == that.getUnknownDurationSecs() &&
                Objects.equals(getUuid(), that.getUuid()) &&
                Objects.equals(getLastKnownState(), that.getLastKnownState()) &&
                Objects.equals(getUtilizationDate(), that.getUtilizationDate()) &&
                Objects.equals(getLastTransitionTime(), that.getLastTransitionTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getIdleDurationSecs(), getBuildingDurationSecs(), getCancelledDurationSecs(), getLostContactDurationSecs(), getUnknownDurationSecs(), getLastKnownState(), getUtilizationDate(), getLastTransitionTime());
    }

    private interface DurationUpdater {
        void update(AgentUtilization utilization, int duration);
    }

    private static class IdleDurationUpdater implements DurationUpdater {
        @Override
        public void update(AgentUtilization utilization, int duration) {
            utilization.setIdleDurationSecs(utilization.getIdleDurationSecs() + duration);
        }
    }

    private static class BuildingDurationUpdater implements DurationUpdater {
        @Override
        public void update(AgentUtilization utilization, int duration) {
            utilization.setBuildingDurationSecs(utilization.getBuildingDurationSecs() + duration);
        }
    }

    private static class UnknownDurationUpdater implements DurationUpdater {
        @Override
        public void update(AgentUtilization utilization, int duration) {
            utilization.setUnknownDurationSecs(utilization.getUnknownDurationSecs() + duration);
        }
    }

    private static class CancelledDurationUpdater implements DurationUpdater {
        @Override
        public void update(AgentUtilization utilization, int duration) {
            utilization.setCancelledDurationSecs(utilization.getCancelledDurationSecs() + duration);
        }
    }

    private static class LostContanctDurationUpdater implements DurationUpdater {
        @Override
        public void update(AgentUtilization utilization, int duration) {
            utilization.setLostContactDurationSecs(utilization.getLostContactDurationSecs() + duration);
        }
    }
}
