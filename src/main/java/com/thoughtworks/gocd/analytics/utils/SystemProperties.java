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

package com.thoughtworks.gocd.analytics.utils;

import com.thoughtworks.go.plugin.api.logging.Logger;

public class SystemProperties {
    public static final Logger LOG = Logger.getLoggerFor(SystemProperties.class);
    private static GoSystemProperty<Integer> ANALYTICS_DB_DATA_PURGE_INTERVAL = new GoIntSystemProperty("analytics.db.data.purge.interval.days", 30);

    public static int getAnalyticsDbDataPurgeInterval() {
        return ANALYTICS_DB_DATA_PURGE_INTERVAL.getValue();
    }

    private static class GoIntSystemProperty extends GoSystemProperty<Integer> {
        public GoIntSystemProperty(String propertyName, Integer defaultValue) {
            super(propertyName, defaultValue);
        }

        @Override
        protected Integer convertValue(String propertyValueFromSystem, Integer defaultValue) {
            return propertyValueFromSystem == null ? defaultValue : Integer.parseInt(propertyValueFromSystem);
        }
    }

    private abstract static class GoSystemProperty<T> {
        private String propertyName;
        private T defaultValue;

        GoSystemProperty(String propertyName, T defaultValue) {
            this.propertyName = propertyName;
            this.defaultValue = defaultValue;
        }

        T getValue() {
            String propertyValue = System.getProperty(propertyName);
            try {
                return convertValue(propertyValue, defaultValue);
            } catch (Exception e) {
                LOG.error("[System-Property] Error converting system property '{}' to type '{}'. Using default value '{}'.", propertyName, defaultValue.getClass(), defaultValue, e);
                return defaultValue;
            }
        }

        protected abstract T convertValue(String propertyValueFromSystem, T defaultValue);

        public String propertyName() {
            return propertyName;
        }
    }
}
