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

package com.thoughtworks.gocd.analytics.dao;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;

interface DAOIntegrationTest {
    ZonedDateTime TEST_TS = ZonedDateTime.parse("2018-03-22T12:34:56Z", DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(UTC);
    String CANCELLED = "Cancelled";
    String PASSED = "Passed";
    String FAILED = "Failed";
    String ENABLED = "enabled";
    String DISABLED = "disabled";
}
