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

import com.thoughtworks.gocd.analytics.utils.Util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeField extends Field {
    public TimeField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }

    @Override
    public String doValidate(String input) {
        if (Util.isBlank(input)) {
            return null;
        }
        try {
            LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return this.displayName + " must be a valid hour and minute of a day, use format 'HH:mm'.";
        }
        return null;
    }
}
