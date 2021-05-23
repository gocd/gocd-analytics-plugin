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

package com.thoughtworks.gocd.analytics.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.utils.ViewUtils;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.util.Collections;

import static com.thoughtworks.gocd.analytics.db.TransactionAware.ERROR_DB_CONNECT_FAILURE;
import static com.thoughtworks.gocd.analytics.db.TransactionAware.ERROR_PLUGIN_NOT_CONFIGURED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AbstractSessionFactoryAwareExecutorTest {

    @Test
    public void executeShouldRespondWithDbConnectionFailureIfDbIsDown() throws Exception {
        AbstractSessionFactoryAwareExecutor executor = new AbstractSessionFactoryAwareExecutor(mock(AnalyticsRequest.class), mock(SessionFactory.class)) {
            @Override
            protected GoPluginApiResponse doExecute() {
                throw new RuntimeException("Help!", new PSQLException("Connection refused", PSQLState.CONNECTION_FAILURE));
            }
        };

        GoPluginApiResponse actualResponse = executor.execute();
        String expectedResponseBody = new AnalyticsResponseBody(Collections.EMPTY_MAP, "error-message.html?msg=" + ViewUtils.jsUriEncode(ERROR_DB_CONNECT_FAILURE + ": Connection refused")).toJson();

        assertEquals(expectedResponseBody, actualResponse.responseBody());
        assertEquals(200, actualResponse.responseCode());
    }

    @Test
    public void executeShouldRespondWithPluginNotConfiguredErrorWhenSessionFactoryMissing() throws Exception {
        AbstractSessionFactoryAwareExecutor executor = new AbstractSessionFactoryAwareExecutor(mock(AnalyticsRequest.class), null) {
            @Override
            protected GoPluginApiResponse doExecute() {
                return null;
            }
        };

        GoPluginApiResponse actualResponse = executor.execute();
        String expectedResponseBody = new AnalyticsResponseBody(Collections.EMPTY_MAP, "error-message.html?msg=" + ViewUtils.jsUriEncode(ERROR_PLUGIN_NOT_CONFIGURED)).toJson();

        assertEquals(expectedResponseBody, actualResponse.responseBody());
        assertEquals(200, actualResponse.responseCode());
    }

    @Test
    public void executeShouldRespondWithPluginErrorPageIfExecuteErrorsOut() throws Exception {
        AbstractSessionFactoryAwareExecutor executor = new AbstractSessionFactoryAwareExecutor(mock(AnalyticsRequest.class), mock(SessionFactory.class)) {
            @Override
            protected GoPluginApiResponse doExecute() {
                throw new RuntimeException("Bomb!");
            }
        };

        GoPluginApiResponse actualResponse = executor.execute();
        String expectedResponseBody = new AnalyticsResponseBody(Collections.EMPTY_MAP, "error-message.html").toJson();

        assertEquals(expectedResponseBody, actualResponse.responseBody());
        assertEquals(200, actualResponse.responseCode());
    }
}
