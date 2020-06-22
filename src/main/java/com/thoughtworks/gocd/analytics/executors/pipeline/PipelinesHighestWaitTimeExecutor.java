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

package com.thoughtworks.gocd.analytics.executors.pipeline;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

public class PipelinesHighestWaitTimeExecutor extends AbstractSessionFactoryAwareExecutor {

    private final PipelineDAO pipelineDAO;

    public PipelinesHighestWaitTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new PipelineDAO(), sessionFactory);
    }

    PipelinesHighestWaitTimeExecutor(AnalyticsRequest analyticsRequest, PipelineDAO pipelineDAO, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.pipelineDAO = pipelineDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        List<Pipeline> pipelines = doInTransaction(new Operation<List<Pipeline>>() {
            final int limit = 10;

            @Override
            public List<Pipeline> execute(SqlSession sqlSession) {
                return pipelineDAO.longestWaiting(sqlSession, startDate(), endDate(), limit);
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(pipelines, "longest-waiting-pipelines-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
