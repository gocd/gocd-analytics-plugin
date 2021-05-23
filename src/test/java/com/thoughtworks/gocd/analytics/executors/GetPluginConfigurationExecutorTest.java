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

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetPluginConfigurationExecutorTest {
    @Test
    public void assertJSONStructure() throws Exception {
        GoPluginApiResponse response = new GetPluginConfigurationExecutor().execute();

        assertEquals(200, response.responseCode());
        JSONAssert.assertEquals(expectedJson(), response.responseBody(), true);
    }

    private String expectedJson() {
        return "{\n" +
                "    \"client_cert\": {\n" +
                "        \"display-name\": \"Client Certificate\",\n" +
                "        \"display-order\": \"8\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"client_key\": {\n" +
                "        \"display-name\": \"Client Key\",\n" +
                "        \"display-order\": \"9\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"client_pkcs8_key\": {\n" +
                "        \"display-name\": \"PKCS8 Client Key\",\n" +
                "        \"display-order\": \"10\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"connection\": {\n" +
                "        \"display-name\": \"DB Connection\",\n" +
                "        \"display-order\": \"11\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"host\": {\n" +
                "        \"display-name\": \"DB Host\",\n" +
                "        \"display-order\": \"0\",\n" +
                "        \"required\": true,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"max_connection_wait_time\": {\n" +
                "        \"display-name\": \"Maximum connection wait time\",\n" +
                "        \"display-order\": \"14\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"max_connections_active\": {\n" +
                "        \"display-name\": \"Maximum Active Connections\",\n" +
                "        \"display-order\": \"12\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"max_connections_idle\": {\n" +
                "        \"display-name\": \"Maximum Idle Connections\",\n" +
                "        \"display-order\": \"13\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"name\": {\n" +
                "        \"display-name\": \"DB Name\",\n" +
                "        \"display-order\": \"4\",\n" +
                "        \"required\": true,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"password\": {\n" +
                "        \"display-name\": \"User Password\",\n" +
                "        \"display-order\": \"3\",\n" +
                "        \"required\": true,\n" +
                "        \"secure\": true\n" +
                "    },\n" +
                "    \"periodic_cleanup_time\": {\n" +
                "        \"display-name\": \"Periodic Cleanup Time\",\n" +
                "        \"display-order\": \"15\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"port\": {\n" +
                "        \"default-value\": \"5432\",\n" +
                "        \"display-name\": \"DB Port\",\n" +
                "        \"display-order\": \"1\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"root_cert\": {\n" +
                "        \"display-name\": \"Root Certificate\",\n" +
                "        \"display-order\": \"7\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"ssl_mode\": {\n" +
                "        \"default-value\": \"verify-full\",\n" +
                "        \"display-name\": \"SSL verification level\",\n" +
                "        \"display-order\": \"6\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"use_ssl\": {\n" +
                "        \"default-value\": \"false\",\n" +
                "        \"display-name\": \"Connect using SSL\",\n" +
                "        \"display-order\": \"5\",\n" +
                "        \"required\": false,\n" +
                "        \"secure\": false\n" +
                "    },\n" +
                "    \"username\": {\n" +
                "        \"display-name\": \"Username\",\n" +
                "        \"display-order\": \"2\",\n" +
                "        \"required\": true,\n" +
                "        \"secure\": false\n" +
                "    }\n" +
                "}\n";
    }
}
