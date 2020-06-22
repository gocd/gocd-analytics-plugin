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

package com.thoughtworks.gocd.analytics.serialization.adapters;

import com.google.gson.*;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;

public class DefaultZonedDateTimeTypeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final DateFormat enUsFormat;
    private final DateFormat localFormat;
    private String datePattern;

    public DefaultZonedDateTimeTypeAdapter() {
        this.datePattern = DATE_PATTERN;
        this.enUsFormat = new SimpleDateFormat(datePattern, Locale.US);
        this.localFormat = new SimpleDateFormat(datePattern);
    }

    // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
    // See issue 162
    @Override
    public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        synchronized (localFormat) {
            String dateFormatAsString = ZonedDateTime.ofInstant(src.toInstant(), UTC).format(DateTimeFormatter.ofPattern(datePattern));
            return new JsonPrimitive(dateFormatAsString);
        }
    }

    @Override
    public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }
        Date date = deserializeToDate(json);
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(), UTC);
    }

    private Date deserializeToDate(JsonElement json) {
        synchronized (localFormat) {
            if (json.getAsString().isEmpty()) {
                return null;
            }
            try {
                return localFormat.parse(json.getAsString());
            } catch (ParseException ignored) {
            }
            try {
                return enUsFormat.parse(json.getAsString());
            } catch (ParseException ignored) {
            }
            try {
                return ISO8601Utils.parse(json.getAsString(), new ParsePosition(0));
            } catch (ParseException e) {
                throw new JsonSyntaxException(json.getAsString(), e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DefaultZonedDateTimeTypeAdapter.class.getSimpleName());
        sb.append('(').append(localFormat.getClass().getSimpleName()).append(')');
        return sb.toString();
    }
}
