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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SystemPropertiesTest {
    private final int defaultAnalyticsDbDataPurgeIntervalInDays = 30;

    @Before
    @After
    public void clearSystemProperties() {
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", "");
    }

    @Test
    public void shouldReturnAnalyticsDbDataPurgeIntervalValueSpecifiedAsSystemProperty() {
        String dataPurgeIntervalInDays = "1";
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", dataPurgeIntervalInDays);

        assertThat(SystemProperties.getAnalyticsDbDataPurgeInterval(), is(Integer.parseInt(dataPurgeIntervalInDays)));
    }

    @Test
    public void shouldReturnDefaultAnalyticsDbDataPurgeIntervalValueWhenNoSystemPropertyIsSpecified() {
        assertThat(SystemProperties.getAnalyticsDbDataPurgeInterval(), is(defaultAnalyticsDbDataPurgeIntervalInDays));
    }

    @Test
    public void shouldReturnDefaultAnalyticsDbDataPurgeIntervalValueWhenInvalidTypeSystemPropertyIsSpecified() {
        String dataPurgeIntervalInDays = "five days";
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", dataPurgeIntervalInDays);

        assertThat(SystemProperties.getAnalyticsDbDataPurgeInterval(), is(defaultAnalyticsDbDataPurgeIntervalInDays));
    }
}
