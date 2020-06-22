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
import org.apache.ibatis.session.SqlSession;

public abstract class TransactionAware {
    public static final String ERROR_PLUGIN_NOT_CONFIGURED = "Could not establish a database connection. Please check the plugin settings to confirm the plugin has been configured correctly.";
    public static final String ERROR_DB_CONNECT_FAILURE = "Failed to connect to analytics database";

    protected SessionFactory sessionFactory;

    public TransactionAware(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public <T> T doInTransaction(final Operation<T> operation) {
        T result;
        try (SqlSession sqlSession = this.sessionFactory.openSession()) {
            try {
                result = operation.execute(sqlSession);
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw e;
            }
            return result;
        }
    }

    public interface Operation<T> {
        T execute(SqlSession sqlSession);
    }
}
