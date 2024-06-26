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

package com.thoughtworks.gocd.analytics.executors.job;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.models.JobTimeSummary;
import java.util.ArrayList;
import java.util.List;

public class PriorityJobExecutor extends AbstractSessionFactoryAwareExecutor {

    private final JobDAO jobDAO;

    public PriorityJobExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new JobDAO(), sessionFactory);
    }

    public PriorityJobExecutor(AnalyticsRequest analyticsRequest, JobDAO jobDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.jobDAO = jobDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {

        final String result = param(PARAM_RESULT);
        final String startDate = param(PARAM_START_DATE);
        final String endDate = param(PARAM_END_DATE);

//        final int pipelineCounterStart = Integer.parseInt(param(PARAM_PIPELINE_COUNTER_START));
//        final int pipelineCounterEnd = Integer.parseInt(param(PARAM_PIPELINE_COUNTER_END));

        List<JobTimeSummary> jobs = doInTransaction(sqlSession -> {
//            if (stageName ==  null || stageName.isEmpty() || stageName.isBlank()) {
//                return new ArrayList<>();
//            }
//            return jobDAO.getAllJobsByStageName(sqlSession, stageName, pipelineCounterStart, pipelineCounterEnd);
            return jobDAO.jobSummary(sqlSession, startDate, endDate, result);
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(jobs,
            "job-timeline-chart.html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
