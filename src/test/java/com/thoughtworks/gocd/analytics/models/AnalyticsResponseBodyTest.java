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

package com.thoughtworks.gocd.analytics.models;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

public class AnalyticsResponseBodyTest {

    @Test
    public void shouldSerializeObjectsWithZonedDateTimeToJSONWithTimeInUTC() throws Exception {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1518783877863L), ZoneId.of("Asia/Kolkata"));
        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(
                Collections.singletonMap("date", now), "path_to_view");

        String expectedJSON = "{" +
                "\"data\": \"{\\\"date\\\":\\\"2018-02-16T12:24:37.863+0000\\\"}\"," +
                "\"view_path\": \"path_to_view\"" +
                "}";

        JSONAssert.assertEquals(expectedJSON, responseBody.toJson(), true);
    }
}
