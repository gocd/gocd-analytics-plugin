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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AgentUtilizationUpdaterTest {

    private AgentUtilizationDAO agentUtilizationDAO;
    private SqlSession sqlSession;
    private ArgumentCaptor<AgentUtilization> captor;

    @Before
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

        assertThat(captor.getValue(), is(expectedUtilization));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getIdleDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getBuildingDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getCancelledDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getLostContactDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getLostContactDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getUnknownDurationSecs(), is((int) Duration.between(lastTransitionTime.toInstant(), currentTransitionTime.toInstant()).getSeconds()));
        assertThat(agentUtilization.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(agentUtilization.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(agentUtilization.getUtilizationDate(), is(lastTransitionTime));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getUnknownDurationSecs(), is(DateUtils.durationTillEndOfDayInSeconds(yesterday)));
        assertThat(agentUtilization.getLastTransitionTime(), is(yesterday));
        assertThat(agentUtilization.getLastKnownState(), is("Unknown"));
        assertThat(agentUtilization.getUtilizationDate(), is(yesterday));

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getValue();
        assertThat(utilizationForCurrentTransitionDate.getUuid(), is(agentStatusRequest.getUuid()));
        assertThat(utilizationForCurrentTransitionDate.getUtilizationDate(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(utilizationForCurrentTransitionDate.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getUnknownDurationSecs(), is(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime())));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getUnknownDurationSecs(), is(DateUtils.durationTillEndOfDayInSeconds(twoDaysAgo)));
        assertThat(agentUtilization.getLastTransitionTime(), is(twoDaysAgo));
        assertThat(agentUtilization.getLastKnownState(), is("Unknown"));
        assertThat(agentUtilization.getUtilizationDate(), is(twoDaysAgo));

        AgentUtilization utilizationForYesterday = captor2.getAllValues().get(0);
        assertThat(utilizationForYesterday.getUuid(), is(agentStatusRequest.getUuid()));
        assertThat(utilizationForYesterday.getUtilizationDate().toEpochSecond(), is(twoDaysAgo.plusDays(1).toEpochSecond()));
        assertThat(utilizationForYesterday.getLastKnownState(), is("Unknown"));
        assertThat(utilizationForYesterday.getLastTransitionTime().toEpochSecond(), is((twoDaysAgo.plusDays(1).toEpochSecond())));

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getAllValues().get(1);
        assertThat(utilizationForCurrentTransitionDate.getUuid(), is(agentStatusRequest.getUuid()));
        assertThat(utilizationForCurrentTransitionDate.getUtilizationDate(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(utilizationForCurrentTransitionDate.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getUnknownDurationSecs(), is(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime())));
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

        assertThat(captor.getValue(), is(agentUtilization));
        assertThat(agentUtilization.getUnknownDurationSecs(), is(DateUtils.durationTillEndOfDayInSeconds(twoDaysAgo)));
        assertThat(agentUtilization.getLastTransitionTime(), is(twoDaysAgo));
        assertThat(agentUtilization.getLastKnownState(), is("Unknown"));
        assertThat(agentUtilization.getUtilizationDate(), is(twoDaysAgo));

        AgentUtilization utilizationForYesterday = captor2.getAllValues().get(0);
        assertThat(utilizationForYesterday.getUuid(), is(agentStatusRequest.getUuid()));
        assertThat(utilizationForYesterday.getUtilizationDate().toEpochSecond(), is(agentStatusRequest.getTransitionTime().minusDays(1).toEpochSecond()));
        assertThat(utilizationForYesterday.getLastKnownState(), is("Unknown"));
        assertThat(utilizationForYesterday.getLastTransitionTime().toEpochSecond(), is((agentStatusRequest.getTransitionTime().minusDays(1).toEpochSecond())));

        AgentUtilization utilizationForCurrentTransitionDate = captor2.getAllValues().get(1);
        assertThat(utilizationForCurrentTransitionDate.getUuid(), is(agentStatusRequest.getUuid()));
        assertThat(utilizationForCurrentTransitionDate.getUtilizationDate(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getLastKnownState(), is(agentStatusRequest.getAgentState()));
        assertThat(utilizationForCurrentTransitionDate.getLastTransitionTime(), is(agentStatusRequest.getTransitionTime()));
        assertThat(utilizationForCurrentTransitionDate.getUnknownDurationSecs(), is(DateUtils.durationFromStartOfDayInSeconds(agentStatusRequest.getTransitionTime())));
    }

    private ZonedDateTime timeWithMinusHours(int hours) {
        ZonedDateTime time = ZonedDateTime.now();
        return time.minusHours(hours).withZoneSameInstant(ZoneId.of("UTC"));
    }
}
