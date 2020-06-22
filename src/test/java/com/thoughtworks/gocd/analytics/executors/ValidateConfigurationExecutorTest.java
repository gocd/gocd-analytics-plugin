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

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.TestConnectionResult;
import com.thoughtworks.gocd.analytics.db.Database;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidateConfigurationExecutorTest {

    @Test
    public void shouldValidateABadConfiguration() throws Exception {
        GoPluginApiRequest goApiRequest = mock(GoPluginApiRequest.class);

        when(goApiRequest.requestBody()).thenReturn("{ \"plugin-settings\": {}}");

        GoPluginApiResponse response = new ValidateConfigurationExecutor(goApiRequest).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"message\": \"DB Host must not be blank.\",\n" +
                "    \"key\": \"host\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Username must not be blank.\",\n" +
                "    \"key\": \"username\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"DB Name must not be blank.\",\n" +
                "    \"key\": \"name\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    public void shouldTestDbConnectionUsingTheConfiguration() throws Exception {
        GoPluginApiRequest goApiRequest = mock(GoPluginApiRequest.class);
        Database database = mock(Database.class);
        String validatePluginSettings = validatePluginSettings("db.example.com", "bob", "p@ssw0rd", "blah");

        when(goApiRequest.requestBody()).thenReturn(validatePluginSettings);
        when(database.testConnection(PluginSettings.fromValidateSettingsJSON(validatePluginSettings))).thenReturn(TestConnectionResult.SUCCESS);

        GoPluginApiResponse response = new ValidateConfigurationExecutor(goApiRequest, database).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldIncludeConnectionErrorsIfTestConnectionFails() throws Exception {
        GoPluginApiRequest goApiRequest = mock(GoPluginApiRequest.class);
        Database database = mock(Database.class);
        String validatePluginSettings = validatePluginSettings("db.example.com", "bob", "p@ssw0rd", "blah");

        when(goApiRequest.requestBody()).thenReturn(validatePluginSettings);
        when(database.testConnection(PluginSettings.fromValidateSettingsJSON(validatePluginSettings))).thenReturn(new TestConnectionResult(false, "Error connecting to db."));

        GoPluginApiResponse response = new ValidateConfigurationExecutor(goApiRequest, database).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[{\"key\": \"connection\",\"message\": \"Error connecting to db.\"}]", response.responseBody(), true);
    }

    private String validatePluginSettings(String host, String username, String password, String dbName) {
        HashMap<String, Map<String, String>> map = new HashMap<>();
        map.put("host", Collections.singletonMap("value", host));
        map.put("username", Collections.singletonMap("value", username));
        map.put("password", Collections.singletonMap("value", password));
        map.put("name", Collections.singletonMap("value", dbName));

        return GSON.toJson(Collections.singletonMap("plugin-settings", map));
    }
}
