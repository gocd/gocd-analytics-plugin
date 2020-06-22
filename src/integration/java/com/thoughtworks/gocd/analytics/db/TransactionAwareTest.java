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

package com.thoughtworks.gocd.analytics.db;

import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TransactionAwareTest {
    private SqlSession sqlSession;
    private SessionFactory sessionFactory;
    private TransactionAware transaction;
    private TestDBConnectionManager manager;

    @Before
    public void before() throws SQLException, InterruptedException {
        manager = new TestDBConnectionManager();
        sessionFactory = manager.getSessionFactory();
        transaction = new TransactionAware(manager.getSessionFactory()) {
        };
        sqlSession = manager.getSqlSession();
    }

    @After
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldSuccessfullyCommitChangesIfThereAreNoErrors() throws InterruptedException {
        PipelineDAO dao = new PipelineDAO();
        List<PipelineInstance> instances = dao.instancesForPipeline(sqlSession, "test", null, ZonedDateTime.now());
        assertThat(instances.size(), is(0));

        transaction.doInTransaction(new TransactionAware.Operation<Boolean>() {
            @Override
            public Boolean execute(SqlSession sqlSession) {
                PipelineInstance instance = new PipelineInstance(1, "test", 1, 1, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
                dao.updateOrInsert(sqlSession, instance);
                return true;
            }
        });
        sqlSession.clearCache();
        List<PipelineInstance> result = dao.instancesForPipeline(sqlSession, "test", null, ZonedDateTime.now());
        assertThat(result.size(), is(1));
    }

    @Test
    public void shouldRollbackIfError() {
        PipelineDAO dao = new PipelineDAO();
        List<PipelineInstance> instances = dao.instancesForPipeline(sqlSession, "test", null, ZonedDateTime.now());
        assertThat(instances.size(), is(0));

        try {
            transaction.doInTransaction(new TransactionAware.Operation<Boolean>() {
                @Override
                public Boolean execute(SqlSession sqlSession) {
                    PipelineInstance instance = new PipelineInstance(1, "test", 1, 1, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
                    dao.updateOrInsert(sqlSession, null);
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
        sqlSession.clearCache();
        List<PipelineInstance> result = dao.instancesForPipeline(sqlSession, "test", null, ZonedDateTime.now());
        assertThat(result.size(), is(0));
    }
}
