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
import com.thoughtworks.gocd.analytics.AnalyticTypes;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.db.TransactionAware;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import com.thoughtworks.gocd.analytics.utils.ResponseMessages;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.gocd.analytics.AnalyticsPlugin.LOG;

public abstract class AbstractSessionFactoryAwareExecutor extends TransactionAware implements RequestExecutor, AnalyticTypes {

    private static final List<String> CONNECTION_ERRORS = Arrays.asList(
            PSQLState.CONNECTION_UNABLE_TO_CONNECT.getState(),
            PSQLState.CONNECTION_FAILURE.getState(),
            PSQLState.CONNECTION_FAILURE_DURING_TRANSACTION.getState(),
            PSQLState.CONNECTION_REJECTED.getState(),
            PSQLState.CONNECTION_DOES_NOT_EXIST.getState()
    );
    private AnalyticsRequest analyticsRequest;

    public AbstractSessionFactoryAwareExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        super(sessionFactory);
        this.analyticsRequest = analyticsRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        String errorMessage = null;

        if (sessionFactory == null) {
            return ResponseMessages.errorMessage(ERROR_PLUGIN_NOT_CONFIGURED);
        }

        try {
            return doExecute();
        } catch (Exception e) {
            LOG.error("[Get-Analytics] Error generating analytics for: {}", analyticsRequest, e);

            final Throwable cause = e.getCause();
            if (cause instanceof PSQLException && isConnectionError(cause)) {
                errorMessage = ERROR_DB_CONNECT_FAILURE + ": " + cause.getMessage();
            }

            return ResponseMessages.errorMessage(errorMessage);
        }
    }

    private boolean isConnectionError(Throwable t) {
        PSQLException pe = (PSQLException) t;
        return CONNECTION_ERRORS.contains(pe.getSQLState());
    }

    protected abstract GoPluginApiResponse doExecute();

    public String param(String name) {
        return analyticsRequest.getParams().get(name);
    }

    /**
     * @return start_date parameter as a ZonedDateTime, defaulting to a week ago if not provided
     */
    public ZonedDateTime startDate() {
        return startDate(7L);
    }

    /**
     * @return start_date parameter as a ZonedDateTime, defaulting to the provided value if not provided
     */
    public ZonedDateTime startDate(Long defaultOffset) {
        final String sd = param(PARAM_START_DATE);
        if ("*".equals(sd)) return null;

        return (null != sd) ? DateUtils.parseDate(sd) : ZonedDateTime.now().minusDays(defaultOffset);
    }

    /**
     * @return end_date parameter as a ZonedDateTime, defaulting to now() if not provided
     */
    public ZonedDateTime endDate() {
        final String ed = param(PARAM_END_DATE);
        if ("*".equals(ed)) return null;

        return (null != ed) ? DateUtils.parseDate(ed) : ZonedDateTime.now();
    }
}
