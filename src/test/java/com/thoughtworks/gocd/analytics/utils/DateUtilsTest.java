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

import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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

        assertThat(DateUtils.durationTillEndOfDayInSeconds(dateTimeInUTC), is(14399));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(dateTimeInIST), is(14399));

        assertThat(DateUtils.durationTillEndOfDayInSeconds(feb28NonLeapYear), is(14399));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(feb29LeapYear), is(14399));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(dec31), is(14399));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(jan1), is(14399));

        assertThat(DateUtils.durationTillEndOfDayInSeconds(midnightMinusTwoSeconds), is(1));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(midnightMinusOneSecond), is(0));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(midnight), is(86399));
        assertThat(DateUtils.durationTillEndOfDayInSeconds(midnightPlusOneSecond), is(86398));
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

        assertThat(DateUtils.durationFromStartOfDayInSeconds(dateTimeInUTC), is(36000));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(dateTimeInIST), is(36000));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(feb28NonLeapYear), is(36000));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(feb29LeapYear), is(36000));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(dec31), is(36000));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(jan1), is(36000));

        assertThat(DateUtils.durationFromStartOfDayInSeconds(midnightPlusTwoSeconds), is(2));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(midnightPlusOneSecond), is(1));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(midnight), is(0));
        assertThat(DateUtils.durationFromStartOfDayInSeconds(midnightMinusOneSecond), is(86399));
    }

    @Test
    public void shouldComputeDurationBetweenDatesInSeconds() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();

        assertThat(DateUtils.durationBetweenInSeconds(now, now.plusSeconds(10)), is(10));

    }
}
