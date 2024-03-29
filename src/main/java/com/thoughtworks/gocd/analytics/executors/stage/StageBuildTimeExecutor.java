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

package com.thoughtworks.gocd.analytics.executors.stage;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.utils.ResponseMessages;
import com.thoughtworks.gocd.analytics.utils.Util;

import java.util.HashMap;
import java.util.List;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

public class StageBuildTimeExecutor extends AbstractSessionFactoryAwareExecutor {
    private StageDAO stageDao;

    public StageBuildTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory, StageDAO stageDao) {
        super(analyticsRequest, sessionFactory);
        this.stageDao = stageDao;
    }

    public StageBuildTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, sessionFactory, new StageDAO());
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        final String pipeline = param(PARAM_PIPELINE_NAME);
        final String stage = param(PARAM_STAGE_NAME);

        if (Util.isEmpty(pipeline) || Util.isEmpty(stage)) {
            return ResponseMessages.infoMessage("You must select a pipeline and stage to view this metric.");
        }

        List<Stage> stages = doInTransaction(sqlSession -> stageDao.stageHistory(sqlSession, pipeline, stage, startDate(30L), endDate()));

        final HashMap<String, Object> data = new HashMap<>();

        data.put("runs", stages);
        data.put("identifier", pipeline + "/" + stage);

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(data, "stage-build-time-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
