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

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.mapper.*;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummary;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class SessionFactory {
    private static final Logger LOG = Logger.getLoggerFor(SessionFactory.class);

    private SqlSessionFactory sessionFactory;

    public void initialize(BasicDataSource basicDataSource) throws InterruptedException {
        LOG.info("Initializing SqlSessionFactory...");

        Environment environment = new Environment("development", new JdbcTransactionFactory(), basicDataSource);
        Configuration configuration = new Configuration(environment);

        configureMappers(configuration);

        sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        LOG.info("Done Initializing SqlSessionFactory");
    }

    public SqlSession openSession() {
        return sessionFactory.openSession();
    }

    private void configureMappers(Configuration configuration) {
        configuration.addMapper(JobMapper.class);
        configuration.addMapper(StageMapper.class);
        configuration.addMapper(PipelineMapper.class);
        configuration.addMapper(AgentMapper.class);
        configuration.addMapper(AgentUtilizationMapper.class);
        configuration.addMapper(AgentTransitionsMapper.class);
        configuration.addMapper(MaterialRevisionMapper.class);
        configuration.addMapper(WorkflowMapper.class);
        configuration.addMapper(PipelineWorkflowMapper.class);
        configuration.addMapper(UniversalMapper.class);
    }
}
