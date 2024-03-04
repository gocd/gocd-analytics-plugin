/*
 * Copyright 2024 ThoughtWorks, Inc.
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
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class ActualPipelineRuntimeExecutor extends AbstractSessionFactoryAwareExecutor {

    private final PipelineDAO pipelineDAO;

    public ActualPipelineRuntimeExecutor(AnalyticsRequest analyticsRequest,
        SessionFactory sessionFactory) {
        this(analyticsRequest, new PipelineDAO(), sessionFactory);
    }

    public ActualPipelineRuntimeExecutor(
        AnalyticsRequest analyticsRequest,
        PipelineDAO pipelineDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.pipelineDAO = pipelineDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {

        final String pipelineName = param(PARAM_PIPELINE_NAME);
        List<PipelineInstance> pipelines = doInTransaction(sqlSession -> {
                if (pipelineName == null || pipelineName.isEmpty() || pipelineName.isBlank()) {
                    return new ArrayList<>();
                }
                return pipelineDAO.allPipelineWithNameAndCounter(sqlSession, pipelineName);
            }
        );

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(pipelines,
            "pipeline-timeline-chart.html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
