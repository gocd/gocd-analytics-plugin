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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

import static com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;

public class PipelineBuildTimeExecutor extends AbstractSessionFactoryAwareExecutor {
    private static final Logger LOG = Logger.getLoggerFor(PipelineBuildTimeExecutor.class);
    private PipelineDAO pipelineDAO;

    public PipelineBuildTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new PipelineDAO(), sessionFactory);
    }

    PipelineBuildTimeExecutor(AnalyticsRequest analyticsRequest, PipelineDAO pipelineDAO, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.pipelineDAO = pipelineDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        String pipelineName = param(PARAM_PIPELINE_NAME);

        List<PipelineInstance> instances = doInTransaction(new Operation<List<PipelineInstance>>() {
            @Override
            public List<PipelineInstance> execute(SqlSession sqlSession) {
                return pipelineDAO.instancesForPipeline(sqlSession, pipelineName, startDate(30L), endDate());
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(new Pipeline(pipelineName, instances), viewPath());

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }

    private String viewPath() {
        return "pipeline-instances-chart.html";
    }
}
