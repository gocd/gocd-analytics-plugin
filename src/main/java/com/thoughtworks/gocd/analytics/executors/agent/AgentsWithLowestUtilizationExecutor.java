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


import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.utils.DbDateFormat;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class AgentsWithLowestUtilizationExecutor extends AbstractSessionFactoryAwareExecutor {

    private final AgentUtilizationDAO agentUtilizationDAO;

    public AgentsWithLowestUtilizationExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new AgentUtilizationDAO(), sessionFactory);
    }

    AgentsWithLowestUtilizationExecutor(AnalyticsRequest analyticsRequest, AgentUtilizationDAO agentUtilizationDAO, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.agentUtilizationDAO = agentUtilizationDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        DbDateFormat dateFormat = new DbDateFormat();

        List<AgentUtilization> agentUtilizations = doInTransaction(new Operation<List<AgentUtilization>>() {
            final int limit = 10;
            final String startDate = param(PARAM_START_DATE);
            final String endDate = param(PARAM_END_DATE);
            final String order = "ASC";

            final String start =
                (startDate == null || startDate.isEmpty()) ? dateFormat.getMonthStart() :
                    startDate;
            final String end = (endDate == null || endDate.isEmpty()) ? dateFormat.getTodaysDate() :
                endDate;

            @Override
            public List<AgentUtilization> execute(SqlSession sqlSession) {
                return agentUtilizationDAO.lowestUtilization(sqlSession, start, end, limit, order);
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(agentUtilizations, "agent-with-lowest-utilization-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
