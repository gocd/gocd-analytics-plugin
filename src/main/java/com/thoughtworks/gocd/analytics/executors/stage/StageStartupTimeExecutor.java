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
import com.thoughtworks.gocd.analytics.utils.DbDateFormat;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class StageStartupTimeExecutor extends AbstractSessionFactoryAwareExecutor {

    private final StageDAO stageDAO;

    public StageStartupTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new StageDAO(), sessionFactory);
    }

    public StageStartupTimeExecutor(AnalyticsRequest analyticsRequest, StageDAO stageDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.stageDAO = stageDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {

        DbDateFormat dateFormat = new DbDateFormat();

        final String pipelineName = param(PARAM_PIPELINE_NAME);
        final String pipelineCounter = param(PARAM_PIPELINE_COUNTER);

        final String startDate = param(PARAM_START_DATE);
        final String endDate = param(PARAM_END_DATE);


        final String requestResult = param(PARAM_RESULT);
        final String requestOrder = param(PARAM_ORDER);
        final String requestLimit = param(PARAM_LIMIT);

        final String result = new String("Any").equals(requestResult) ? null : requestResult;
        final String order = new String("ASC").equals(requestOrder) ? null : "DESC";
        final int limit = requestLimit == null ? 1 : Integer.parseInt(requestLimit);


        final String start =
            (startDate == null || startDate.isEmpty()) ? dateFormat.getMonthStart() :
                startDate;
        final String end = (endDate == null || endDate.isEmpty()) ? dateFormat.getTodaysDate() :
            endDate;

        final int counter = pipelineCounter == null ? 0 : Integer.parseInt(pipelineCounter);

        List<Stage> stages = doInTransaction(new Operation<List<Stage>>() {
            @Override
            public List<Stage> execute(SqlSession sqlSession) {
                if (counter == 0) {
                    return stageDAO.getStageStartupTime(sqlSession, start, end, pipelineName,
                            result, order, limit);
                }
                return stageDAO.getStageStartupTimeCompare(sqlSession, pipelineName, counter);
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(stages,
            "stage-startup-chart.html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
