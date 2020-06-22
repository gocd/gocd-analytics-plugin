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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.models.AgentStatusRequest;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import org.apache.ibatis.session.SqlSession;

import java.util.List;


public class AgentUtilizationUpdater {

    private static final Logger LOG = Logger.getLoggerFor(AgentUtilizationUpdater.class);
    private final AgentUtilizationDAO agentUtilizationDAO;

    public AgentUtilizationUpdater() {
        this(new AgentUtilizationDAO());
    }

    protected AgentUtilizationUpdater(AgentUtilizationDAO agentUtilizationDAO) {
        this.agentUtilizationDAO = agentUtilizationDAO;
    }

    public void update(SqlSession sqlSession, AgentStatusRequest agentStatus) {
        AgentUtilization utilizationForCurrentTransitionDate = utilizationForCurrentTransitionDate(sqlSession, agentStatus);

        if (utilizationForCurrentTransitionDate != null) {
            LOG.debug("[Agent-Status] Updating Agent Utilization for agent uuid: {} for utilization date: {}", agentStatus.getUuid(), agentStatus.getTransitionTime());
            this.agentUtilizationDAO.update(sqlSession, utilizationForCurrentTransitionDate.update(agentStatus.getAgentState(),
                    agentStatus.getTransitionTime()));
        } else {
            createAgentUtilization(sqlSession, agentStatus);
        }
    }

    private void createAgentUtilization(SqlSession sqlSession, AgentStatusRequest agentStatus) {
        AgentUtilization lastKnownUtilization = lastKnownUtilizationFor(sqlSession, agentStatus.getUuid());

        if (lastKnownUtilization == null) {
            LOG.debug("[Agent-Status] No previous Agent Utilization found for agent uuid: {} inserting new utilization for transition date: {}", agentStatus.getUuid(), agentStatus.getTransitionTime());
            this.agentUtilizationDAO.insert(sqlSession, AgentUtilization.create(agentStatus.getUuid(),
                    agentStatus.getAgentState(), agentStatus.getTransitionTime()));
        } else {
            LOG.debug("[Agent-Status] Update/Insert of Agent Utilization for agent uuid: {} from last known utilization date: {} to current transition time: {}",
                    agentStatus.getUuid(), lastKnownUtilization.getUtilizationDate(), agentStatus.getTransitionTime());
            this.agentUtilizationDAO.update(sqlSession, lastKnownUtilization.updateUtilizationTillEOD());

            List<AgentUtilization> utilizations = AgentUtilization.createUtilizationsPostLastKnownUtilization(lastKnownUtilization,
                    agentStatus.getUuid(), agentStatus.getAgentState(), agentStatus.getTransitionTime());
            utilizations.stream().forEach(agentUtilization -> this.agentUtilizationDAO.insert(sqlSession, agentUtilization));
        }
    }

    private AgentUtilization utilizationForCurrentTransitionDate(SqlSession sqlSession, AgentStatusRequest agentStatus) {
        return this.agentUtilizationDAO.findUtilization(sqlSession, agentStatus.getUuid(), agentStatus.getTransitionTime());
    }

    private AgentUtilization lastKnownUtilizationFor(SqlSession sqlSession, String uuid) {
        return this.agentUtilizationDAO.findLatestUtilization(sqlSession, uuid);
    }
}
