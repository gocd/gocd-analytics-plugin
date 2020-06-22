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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentDAO;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.db.TransactionAware;
import com.thoughtworks.gocd.analytics.executors.RequestExecutor;
import com.thoughtworks.gocd.analytics.models.Agent;
import com.thoughtworks.gocd.analytics.models.AgentStatusRequest;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessage;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthScope;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthState;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;

import static java.text.MessageFormat.format;

public class AgentStatusRequestExecutor extends TransactionAware implements RequestExecutor {
    private static final Logger LOG = Logger.getLoggerFor(AgentStatusRequestExecutor.class);
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private final AgentStatusRequest request;
    private final AgentDAO agentDAO;
    private final AgentTransitionDAO agentTransitionDAO;
    private final AgentUtilizationUpdater agentUtilizationUpdater;
    private final PluginHealthMessageService pluginHealthMessageService;

    public AgentStatusRequestExecutor(GoPluginApiRequest request, SessionFactory sessionFactory, PluginHealthMessageService pluginHealthMessageService) {
        this(request, sessionFactory, new AgentDAO(), new AgentTransitionDAO(), new AgentUtilizationUpdater(), pluginHealthMessageService);
    }

    protected AgentStatusRequestExecutor(GoPluginApiRequest request, SessionFactory sessionFactory, AgentDAO agentDAO, AgentTransitionDAO agentTransitionDAO,
                                         AgentUtilizationUpdater agentUtilizationUpdater, PluginHealthMessageService pluginHealthMessageService) {
        super(sessionFactory);

        LOG.debug("[Agent-Status] Received request: {}", request.requestBody());
        this.pluginHealthMessageService = pluginHealthMessageService;
        this.request = AgentStatusRequest.fromJSON(request.requestBody());
        this.agentDAO = agentDAO;
        this.agentTransitionDAO = agentTransitionDAO;
        this.agentUtilizationUpdater = agentUtilizationUpdater;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        if (cannotConnectToDB()) {
            PluginHealthMessage message = PluginHealthMessage.warn("No active connection to PostgresDb, cannot store agent status information.");
            pluginHealthMessageService.update(new PluginHealthState(message, PluginHealthScope.forPluginSettings()));

            return successResponse();
        }

        Agent agent = agentFrom(this.request);
        AgentTransition agentTransition = AgentTransition.fromRequest(this.request);

        synchronized (agentMutex(agent.getUuid())) {
            try {
                pluginHealthMessageService.removeByScope(PluginHealthScope.forAgentStatusNotification());
                doInTransaction(new Operation<Boolean>() {
                    @Override
                    public Boolean execute(SqlSession sqlSession) {
                        agentDAO.updateOrInsert(sqlSession, agent);
                        agentUtilizationUpdater.update(sqlSession, request);
                        agentTransitionDAO.insertTransition(sqlSession, agentTransition);
                        return true;
                    }
                });
            } catch (Exception e) {
                LOG.error("[Agent-Status] Error persisting Agent and Agent Utilization for request: {}", request, e);
                PluginHealthMessage error = PluginHealthMessage.error(format("Error storing Agent status data, reason: {0}", e.getMessage()));
                pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forAgentStatusNotification()));
            }
        }

        return successResponse();
    }

    private String agentMutex(String uuid) {
        return (AgentStatusRequestExecutor.class + "_agentupdate_" + uuid.toLowerCase()).intern();
    }

    private Agent agentFrom(AgentStatusRequest agentStatusRequest) {
        Agent agent = new Agent();
        agent.setUuid(agentStatusRequest.getUuid());
        agent.setIsElastic(agentStatusRequest.isElastic());
        agent.setFreeSpace(agentStatusRequest.getFreeSpace());
        agent.setHostName(agentStatusRequest.getHostName());
        agent.setIpAddress(agentStatusRequest.getIpAddress());
        agent.setOperatingSystem(agentStatusRequest.getOperatingSystem());
        agent.setConfigState(agentStatusRequest.getAgentConfigState());

        return agent;
    }

    private GoPluginApiResponse successResponse() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(Collections.singletonMap("status", "success")));
    }

    private boolean cannotConnectToDB() {
        return sessionFactory == null;
    }
}
