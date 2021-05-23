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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SystemPropertiesTest {
    private final int defaultAnalyticsDbDataPurgeIntervalInDays = 30;

    @BeforeEach
    @AfterEach
    public void clearSystemProperties() {
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", "");
    }

    @Test
    public void shouldReturnAnalyticsDbDataPurgeIntervalValueSpecifiedAsSystemProperty() {
        String dataPurgeIntervalInDays = "1";
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", dataPurgeIntervalInDays);

        assertEquals(Integer.parseInt(dataPurgeIntervalInDays), SystemProperties.getAnalyticsDbDataPurgeInterval());
    }

    @Test
    public void shouldReturnDefaultAnalyticsDbDataPurgeIntervalValueWhenNoSystemPropertyIsSpecified() {
        assertEquals(defaultAnalyticsDbDataPurgeIntervalInDays, SystemProperties.getAnalyticsDbDataPurgeInterval());
    }

    @Test
    public void shouldReturnDefaultAnalyticsDbDataPurgeIntervalValueWhenInvalidTypeSystemPropertyIsSpecified() {
        String dataPurgeIntervalInDays = "five days";
        System.getProperties().setProperty("analytics.db.data.purge.interval.days", dataPurgeIntervalInDays);

        assertEquals(defaultAnalyticsDbDataPurgeIntervalInDays, SystemProperties.getAnalyticsDbDataPurgeInterval());
    }
}
