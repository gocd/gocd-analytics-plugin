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

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IntegerFieldTest {
    @Test
    public void shouldValidateThatAFieldIsEitherEmptyOrHasAnIntegerValue() {
        IntegerField field = new IntegerField("key1", "display1", "default1", false, false, "1");

        assertThat(field.doValidate(""), is(nullValue()));
        assertThat(field.doValidate("not-a-number"), is("display1 must be an integer."));
        assertThat(field.doValidate("10"), is(nullValue()));
        assertThat(field.doValidate("-10"), is(nullValue()));
    }
}
