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

package com.thoughtworks.gocd.analytics.executors.vsm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Stage;

import java.util.HashMap;
import java.util.List;

public class VSMWorkflowTimeDistributionExecutor extends AbstractSessionFactoryAwareExecutor {
    private StageDAO stageDAO;
    static final Gson GSON = new GsonBuilder().create();

    public VSMWorkflowTimeDistributionExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, sessionFactory, new StageDAO());
    }

    VSMWorkflowTimeDistributionExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory, StageDAO stageDAO) {
        super(analyticsRequest, sessionFactory);
        this.stageDAO = stageDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        List<Stage> stages = doInTransaction(sqlSession -> stageDAO.allStagesWithWorkflowIdInPipelines(sqlSession,
                Long.valueOf(param(PARAM_VSM_WORKFLOW_ID)), pipelinesInWorkflow()));

        HashMap<String, Object> response = new HashMap<>();
        response.put("stages", stages);
        response.put("pipelines_in_workflow", pipelinesInWorkflow());

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(response, viewPath());
        return new DefaultGoPluginApiResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }

    private List<String> pipelinesInWorkflow() {
        return GSON.fromJson(param(PARAM_PIPELINES_IN_WORKFLOW), new TypeToken<List<String>>(){}.getType());
    }
    private String viewPath() {
        return "workflow-time-distribution-chart.html";
    }
}
