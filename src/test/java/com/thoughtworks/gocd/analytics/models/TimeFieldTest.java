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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TimeFieldTest {
    @Test
    public void shouldValidateTimeFieldIsInValidTimeFormat() throws Exception {
        TimeField field = new TimeField("key1", "display1", "default1", false, false, "1");

        assertNull(field.doValidate("20:00"));
        assertEquals("display1 must be a valid hour and minute of a day, use format 'HH:mm'.", field.doValidate("20:00:00"));
        assertEquals("display1 must be a valid hour and minute of a day, use format 'HH:mm'.", field.doValidate("30:00"));
        assertEquals("display1 must be a valid hour and minute of a day, use format 'HH:mm'.", field.doValidate("10:60"));
    }
}
