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

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class DateUtils {
    public static final ZoneId UTC = ZoneId.of("UTC");

    public static ZonedDateTime parseDate(String isoDate) {
        return LocalDate.parse(isoDate, ISO_LOCAL_DATE).atStartOfDay(ZoneId.systemDefault());
    }

    public static int durationTillEndOfDayInSeconds(ZonedDateTime zonedDateTime) {
        ZonedDateTime midnight = zonedDateTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        return Math.toIntExact(zonedDateTime.until(midnight.minusSeconds(1), ChronoUnit.SECONDS));
    }

    public static int durationFromStartOfDayInSeconds(ZonedDateTime zonedDateTime) {
        ZonedDateTime startOfDay = zonedDateTime.truncatedTo(ChronoUnit.DAYS);

        return Math.toIntExact(startOfDay.until(zonedDateTime, ChronoUnit.SECONDS));
    }

    public static int durationBetweenInSeconds(ZonedDateTime start, ZonedDateTime end) {
        return Math.toIntExact(Duration.between(start.toInstant(), end.toInstant()).getSeconds());
    }

    public static ZonedDateTime nowInUTC() {
        return ZonedDateTime.now().withZoneSameInstant(UTC);
    }
}
