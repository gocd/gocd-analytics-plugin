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

package com.thoughtworks.gocd.analytics.executors.agent;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

public class AgentStateTransitionExecutor extends AbstractSessionFactoryAwareExecutor {
    private final AgentTransitionDAO agentTransitionDAO;

    public AgentStateTransitionExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new AgentTransitionDAO(), sessionFactory);
    }

    public AgentStateTransitionExecutor(AnalyticsRequest analyticsRequest, AgentTransitionDAO agentTransitionDAO, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.agentTransitionDAO = agentTransitionDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        List<AgentTransition> agentTransitions = doInTransaction(new Operation<List<AgentTransition>>() {

            @Override
            public List<AgentTransition> execute(SqlSession sqlSession) {
                return agentTransitionDAO.findByUuid(sqlSession, startDate(30L), endDate(), param(PARAM_AGENT_UUID));
            }
        });

        HashMap<String, Object> data = new HashMap<>();
        data.put("uuid", param(PARAM_AGENT_UUID));
        data.put("transitions", agentTransitions);

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(data, "agent-state-transition-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
