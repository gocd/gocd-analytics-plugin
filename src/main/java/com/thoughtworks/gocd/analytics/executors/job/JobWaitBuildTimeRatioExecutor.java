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

package com.thoughtworks.gocd.analytics.executors.job;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Job;
import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class JobWaitBuildTimeRatioExecutor extends AbstractSessionFactoryAwareExecutor {

    private final JobDAO jobDAO;

    public JobWaitBuildTimeRatioExecutor(AnalyticsRequest analyticsRequest,
        SessionFactory sessionFactory) {
        this(analyticsRequest, new JobDAO(), sessionFactory);
    }

    public JobWaitBuildTimeRatioExecutor(AnalyticsRequest analyticsRequest, JobDAO jobDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.jobDAO = jobDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
//        final String pipeline = param(PARAM_PIPELINE_NAME);
//        final String stage = param(PARAM_STAGE_NAME);
//        final String job = param(PARAM_JOB_NAME);

        final int percentage = param(PARAM_PERCENTAGE) == null ? 0 :
            Integer.parseInt(param(PARAM_PERCENTAGE)) / 100;

        List<Job> jobs = doInTransaction(new Operation<List<Job>>() {
            @Override
            public List<Job> execute(SqlSession sqlSession) {
                return jobDAO.jobWaitBuildRatio(sqlSession, percentage);
            }
        });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(jobs,
            "wait-build-ratio-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
