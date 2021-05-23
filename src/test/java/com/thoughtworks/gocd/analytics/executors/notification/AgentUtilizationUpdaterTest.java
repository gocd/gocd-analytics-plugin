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

package com.thoughtworks.gocd.analytics.executors.notification;

import com.thoughtworks.gocd.analytics.AgentStatusRequestMother;
import com.thoughtworks.gocd.analytics.AgentUtilizationMother;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.models.AgentStatusRequest;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AgentUtilizationUpdaterTest {

    private AgentUtilizationDAO agentUtilizationDAO;
    private SqlSession sqlSession;
    private ArgumentCaptor<AgentUtilization> captor;

    @BeforeEach
    public void setUp() throws Exception {
        agentUtilizationDAO = mock(AgentUtilizationDAO.class);
        sqlSession = mock(SqlSession.class);
        captor = ArgumentCaptor.forClass(AgentUtilization.class);
    }

    @Test
    public void shouldInsertAgentUtilizationForAFirstTimeAgentStatusUpdate() throws Exception {
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", ZonedDateTime.now());

        doNothing().when(agentUtilizationDAO).insert(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        AgentUtilization expectedUtilization = new AgentUtilizationMother().withUuid("uuid")
                .withLastKnownState("idle")
                .withLastTransitionTime(agentStatusRequest.getTransitionTime())
                .withUtilizationDate(agentStatusRequest.getTransitionTime())
                .create();

        assertEquals(expectedUtilization, captor.getValue());
    }

    @Test
    public void shouldUpdateIdleDurationIfTransitioningFromIdleState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("idle")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).withUnknownDuration(100).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "building", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getIdleDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateBuildingDurationIfTransitioningFromBuildingState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("BUILDING")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).withUnknownDuration(100).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getBuildingDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateCancelledDurationIfTransitioningFromCancelledState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Cancelled")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getCancelledDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateLostContactDurationIfTransitioningFromLostContactState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("LostContact")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getLostContactDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateLostContactDurationIfTransitioningFromMissingState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Missing")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getLostContactDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateLostContactDurationIfTransitioningFromUnknownState() throws Exception {
        ZonedDateTime lastTransitionTime = timeWithMinusHours(5);
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Unknown")
                .withLastTransitionTime(lastTransitionTime).withUtilizationDate(lastTransitionTime).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds(), agentUtilization.getUnknownDurationSecs());
        assertEquals(agentStatusRequest.getTransitionTime(), agentUtilization.getLastTransitionTime());
        assertEquals(agentStatusRequest.getAgentState(), agentUtilization.getLastKnownState());
        assertEquals(lastTransitionTime, agentUtilization.getUtilizationDate());
    }

    @Test
    public void shouldUpdateLastKnownUtilizationAndCreateNewUtilizationIfAgentStateTransitioningOverADay() throws Exception {
        ArgumentCaptor<AgentUtilization> captor2 = ArgumentCaptor.forClass(AgentUtilization.class);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime yesterday = now.minusDays(1).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime currentTransitionTime = ZonedDateTime.now();

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Unknown")
                .withLastTransitionTime(yesterday).withUtilizationDate(yesterday).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(null);
        when(agentUtilizationDAO.findLatestUtilization(sqlSession, agentUtilization.getUuid())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());
        doNothing().when(agentUtilizationDAO).insert(eq(sqlSession), captor2.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals(DateUtils.durationTillEndOfDayInSeconds(yesterday), agentUtilization.getUnknownDurationSecs());
        assertEquals(yesterday, agentUtilization.getLastTransitionTime());
        assertEquals("Unknown", agentUtilization.getLastKnownState());
        assertEquals(yesterday, agentUtilization.getUtilizationDate());

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getValue();
        assertEquals(agentStatusRequest.getUuid(), utilizationForCurrentTransitionDate.getUuid());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getUtilizationDate());
        assertEquals(agentStatusRequest.getAgentState(), utilizationForCurrentTransitionDate.getLastKnownState());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getLastTransitionTime());
        assertEquals(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime()), utilizationForCurrentTransitionDate.getUnknownDurationSecs());
    }

    @Test
    public void shouldUpdateLastKnownUtilizationAndCreateMultipleNewUtilizationIfAgentStateTransitioningOverMultipleDays() throws Exception {
        ArgumentCaptor<AgentUtilization> captor2 = ArgumentCaptor.forClass(AgentUtilization.class);
        ZonedDateTime currentTransitionTime = ZonedDateTime.parse("2018-04-15T11:35:58.688+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime twoDaysAgo = ZonedDateTime.parse("2018-04-13T22:25:29.165+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Unknown")
                .withLastTransitionTime(twoDaysAgo).withUtilizationDate(twoDaysAgo).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(null);
        when(agentUtilizationDAO.findLatestUtilization(sqlSession, agentUtilization.getUuid())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());
        doNothing().when(agentUtilizationDAO).insert(eq(sqlSession), captor2.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals(DateUtils.durationTillEndOfDayInSeconds(twoDaysAgo), agentUtilization.getUnknownDurationSecs());
        assertEquals(twoDaysAgo, agentUtilization.getLastTransitionTime());
        assertEquals("Unknown", agentUtilization.getLastKnownState());
        assertEquals(twoDaysAgo, agentUtilization.getUtilizationDate());

        AgentUtilization utilizationForYesterday = captor2.getAllValues().get(0);
        assertEquals(agentStatusRequest.getUuid(), utilizationForYesterday.getUuid());
        assertEquals(twoDaysAgo.plusDays(1).toEpochSecond(), utilizationForYesterday.getUtilizationDate().toEpochSecond());
        assertEquals("Unknown", utilizationForYesterday.getLastKnownState());
        assertEquals((twoDaysAgo.plusDays(1).toEpochSecond()), utilizationForYesterday.getLastTransitionTime().toEpochSecond());

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getAllValues().get(1);
        assertEquals(agentStatusRequest.getUuid(), utilizationForCurrentTransitionDate.getUuid());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getUtilizationDate());
        assertEquals(agentStatusRequest.getAgentState(), utilizationForCurrentTransitionDate.getLastKnownState());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getLastTransitionTime());
        assertEquals(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime()), utilizationForCurrentTransitionDate.getUnknownDurationSecs());
    }

    @Test
    public void shouldUpdateLastKnownUtilizationAndCreateMultipleNewUtilizationIfAgentStateTransitioningOverMultipleDaysBetweenDifferentMonths() throws Exception {
        ArgumentCaptor<AgentUtilization> captor2 = ArgumentCaptor.forClass(AgentUtilization.class);
        ZonedDateTime currentTransitionTime = ZonedDateTime.parse("2018-03-01T20:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime twoDaysAgo = currentTransitionTime.minusDays(2).withZoneSameInstant(ZoneId.of("UTC"));

        AgentUtilization agentUtilization = new AgentUtilizationMother().withUuid("uuid").withLastKnownState("Unknown")
                .withLastTransitionTime(twoDaysAgo).withUtilizationDate(twoDaysAgo).create();
        AgentStatusRequest agentStatusRequest = AgentStatusRequestMother.agentWith("uuid", "idle", currentTransitionTime);

        when(agentUtilizationDAO.findUtilization(sqlSession, agentUtilization.getUuid(), agentStatusRequest.getTransitionTime())).thenReturn(null);
        when(agentUtilizationDAO.findLatestUtilization(sqlSession, agentUtilization.getUuid())).thenReturn(agentUtilization);
        doNothing().when(agentUtilizationDAO).update(eq(sqlSession), captor.capture());
        doNothing().when(agentUtilizationDAO).insert(eq(sqlSession), captor2.capture());

        new AgentUtilizationUpdater(agentUtilizationDAO).update(sqlSession, agentStatusRequest);

        assertEquals(agentUtilization, captor.getValue());
        assertEquals(DateUtils.durationTillEndOfDayInSeconds(twoDaysAgo), agentUtilization.getUnknownDurationSecs());
        assertEquals(twoDaysAgo, agentUtilization.getLastTransitionTime());
        assertEquals("Unknown", agentUtilization.getLastKnownState());
        assertEquals(twoDaysAgo, agentUtilization.getUtilizationDate());

        AgentUtilization utilizationForYesterday = captor2.getAllValues().get(0);
        assertEquals(agentStatusRequest.getUuid(), utilizationForYesterday.getUuid());
        assertEquals(agentStatusRequest.getTransitionTime().minusDays(1).toEpochSecond(), utilizationForYesterday.getUtilizationDate().toEpochSecond());
        assertEquals("Unknown", utilizationForYesterday.getLastKnownState());
        assertEquals((agentStatusRequest.getTransitionTime().minusDays(1).toEpochSecond()), utilizationForYesterday.getLastTransitionTime().toEpochSecond());

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getAllValues().get(1);
        assertEquals(agentStatusRequest.getUuid(), utilizationForCurrentTransitionDate.getUuid());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getUtilizationDate());
        assertEquals(agentStatusRequest.getAgentState(), utilizationForCurrentTransitionDate.getLastKnownState());
        assertEquals(agentStatusRequest.getTransitionTime(), utilizationForCurrentTransitionDate.getLastTransitionTime());
        assertEquals(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime()), utilizationForCurrentTransitionDate.getUnknownDurationSecs());
    }

    private ZonedDateTime timeWithMinusHours(int hours) {
        ZonedDateTime time = ZonedDateTime.now();
        return time.minusHours(hours).withZoneSameInstant(ZoneId.of("UTC"));
    }
}
