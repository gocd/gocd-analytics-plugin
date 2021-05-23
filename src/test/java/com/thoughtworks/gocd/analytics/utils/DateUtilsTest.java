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

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {
    @Test
    public void shouldComputeDurationTillEndOfDayInSeconds() throws Exception {
        ZonedDateTime dateTimeInUTC = ZonedDateTime.parse("2018-02-28T20:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime dateTimeInIST = ZonedDateTime.parse("2018-02-28T20:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        ZonedDateTime feb28NonLeapYear = ZonedDateTime.parse("2011-02-28T20:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime feb29LeapYear = ZonedDateTime.parse("2012-02-29T20:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime dec31 = ZonedDateTime.parse("2011-12-31T20:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime jan1 = ZonedDateTime.parse("2011-01-01T20:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        ZonedDateTime midnightMinusTwoSeconds = ZonedDateTime.parse("2011-01-01T23:59:58.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnightMinusOneSecond = ZonedDateTime.parse("2011-01-01T23:59:59.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnight = ZonedDateTime.parse("2011-01-01T00:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnightPlusOneSecond = ZonedDateTime.parse("2011-01-01T00:00:01.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(dateTimeInUTC));
        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(dateTimeInIST));

        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(feb28NonLeapYear));
        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(feb29LeapYear));
        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(dec31));
        assertEquals(14399, DateUtils.durationTillEndOfDayInSeconds(jan1));

        assertEquals(1, DateUtils.durationTillEndOfDayInSeconds(midnightMinusTwoSeconds));
        assertEquals(0, DateUtils.durationTillEndOfDayInSeconds(midnightMinusOneSecond));
        assertEquals(86399, DateUtils.durationTillEndOfDayInSeconds(midnight));
        assertEquals(86398, DateUtils.durationTillEndOfDayInSeconds(midnightPlusOneSecond));
    }

    @Test
    public void shouldComputedurationFromStartOfDayInSeconds() throws Exception {
        ZonedDateTime dateTimeInUTC = ZonedDateTime.parse("2011-07-13T10:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime dateTimeInIST = ZonedDateTime.parse("2011-07-13T10:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime feb28NonLeapYear = ZonedDateTime.parse("2011-02-28T10:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime feb29LeapYear = ZonedDateTime.parse("2012-02-29T10:00:00.000+0530", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime dec31 = ZonedDateTime.parse("2011-12-31T10:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime jan1 = ZonedDateTime.parse("2011-01-01T10:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        ZonedDateTime midnightMinusOneSecond = ZonedDateTime.parse("2011-01-01T23:59:59.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnight = ZonedDateTime.parse("2011-01-01T00:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnightPlusOneSecond = ZonedDateTime.parse("2011-01-01T00:00:01.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        ZonedDateTime midnightPlusTwoSeconds = ZonedDateTime.parse("2011-01-01T00:00:02.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(dateTimeInUTC));
        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(dateTimeInIST));
        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(feb28NonLeapYear));
        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(feb29LeapYear));
        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(dec31));
        assertEquals(36000, DateUtils.durationFromStartOfDayInSeconds(jan1));

        assertEquals(2, DateUtils.durationFromStartOfDayInSeconds(midnightPlusTwoSeconds));
        assertEquals(1, DateUtils.durationFromStartOfDayInSeconds(midnightPlusOneSecond));
        assertEquals(0, DateUtils.durationFromStartOfDayInSeconds(midnight));
        assertEquals(86399, DateUtils.durationFromStartOfDayInSeconds(midnightMinusOneSecond));
    }

    @Test
    public void shouldComputeDurationBetweenDatesInSeconds() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();

        assertEquals(10, DateUtils.durationBetweenInSeconds(now, now.plusSeconds(10)));

    }
}
