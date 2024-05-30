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

package com.thoughtworks.gocd.analytics.executors;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.DBInfoDAO;
import com.thoughtworks.gocd.analytics.db.TransactionAware;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.UniversalSummary;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class DBInfoExecutor extends AbstractSessionFactoryAwareExecutor {

    private final DBInfoDAO dbInfoDAO;

    public DBInfoExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new DBInfoDAO(), sessionFactory);
    }

    public DBInfoExecutor(AnalyticsRequest analyticsRequest, DBInfoDAO dbInfoDAO,
        SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.dbInfoDAO = dbInfoDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        List<UniversalSummary> universalSummaryList = doInTransaction(
            new Operation<List<UniversalSummary>>(){
                @Override
                public List<UniversalSummary> execute(SqlSession sqlSession) {
                    return dbInfoDAO.getAllDB(sqlSession);
                }
            });

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(universalSummaryList,
            "db-info-chart.html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
