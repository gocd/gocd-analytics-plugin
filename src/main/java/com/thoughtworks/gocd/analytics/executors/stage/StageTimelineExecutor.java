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

package com.thoughtworks.gocd.analytics.executors.stage;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Stage;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class StageTimelineExecutor extends AbstractSessionFactoryAwareExecutor {

    private final StageDAO stageDAO;

    public StageTimelineExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new StageDAO(), sessionFactory);
    }

    public StageTimelineExecutor(AnalyticsRequest analyticsRequest, StageDAO stageDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.stageDAO = stageDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {

        final String pipelineName = param(PARAM_PIPELINE_NAME);
        final String requestResult = param(PARAM_RESULT);

        List<Stage> stages = doInTransaction(new Operation<List<Stage>>() {
            @Override
            public List<Stage> execute(SqlSession sqlSession) {
                if (pipelineName ==  null || pipelineName.isEmpty() || pipelineName.isBlank()) {
                    return new ArrayList<>();
                }
//                if (requestResult.equalsIgnoreCase("any")) {
                    return stageDAO.getAllStagesByPipelineNameAndCounter(sqlSession, pipelineName);
//                } else {
//                }
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(stages,
            "stage-timeline-chart.html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
