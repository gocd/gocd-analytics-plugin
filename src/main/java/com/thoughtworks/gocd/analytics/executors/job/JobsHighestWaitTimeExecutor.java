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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.Job;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;

public class JobsHighestWaitTimeExecutor extends AbstractSessionFactoryAwareExecutor {

    private final JobDAO jobDao;

    public JobsHighestWaitTimeExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new JobDAO(), sessionFactory);
    }

    JobsHighestWaitTimeExecutor(AnalyticsRequest analyticsRequest, JobDAO jobDao, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.jobDao = jobDao;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        final String pipelineName = param(PARAM_PIPELINE_NAME);
        final String startDate = param(PARAM_START_DATE);
        final String endDate = param(PARAM_END_DATE);
        final int limit = 10;

        List<Job> jobs = doInTransaction(new Operation<List<Job>>() {
            @Override
            public List<Job> execute(SqlSession sqlSession) {
                return jobDao.longestWaitingFor(sqlSession, pipelineName, startDate, endDate, limit);
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put(PARAM_PIPELINE_NAME, pipelineName);
        data.put(PARAM_START_DATE, startDate);
        data.put(PARAM_END_DATE, endDate);
        data.put("jobs", jobs);


        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(data, "job-chart.html");

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }
}
