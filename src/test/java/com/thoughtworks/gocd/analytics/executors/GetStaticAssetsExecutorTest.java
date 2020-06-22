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

package com.thoughtworks.gocd.analytics.executors;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class GetStaticAssetsExecutorTest {
    @Test
    public void shouldSendBase64Payload() throws Exception {
        GetStaticAssetsExecutor executor = new GetStaticAssetsExecutor("/hello.txt");
        String expected = Base64.getEncoder().encodeToString("hi there!".getBytes(StandardCharsets.ISO_8859_1));
        assertEquals(String.format("{\"assets\":\"%s\"}", expected), executor.execute().responseBody());
    }
}
