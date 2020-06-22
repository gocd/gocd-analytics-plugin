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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.models.*;

import java.util.LinkedHashMap;
import java.util.Map;


public class GetPluginConfigurationExecutor implements RequestExecutor {

    public static final Field USE_SSL = new Field("use_ssl", "Connect using SSL", "false", false, false, "5");
    public static final Field ROOT_CERT = new Field("root_cert", "Root Certificate", null, false, false, "7");
    public static final Field CLIENT_CERT = new Field("client_cert", "Client Certificate", null, false, false, "8");
    public static final Field CLIENT_KEY = new Field("client_key", "Client Key", null, false, false, "9");
    public static final Field CLIENT_PKCS8_KEY = new Field("client_pkcs8_key", "PKCS8 Client Key", null, false, false, "10");
    static final Map<String, Field> FIELDS = new LinkedHashMap<>();
    private static final Field DB_HOST = new NonBlankField("host", "DB Host", null, true, false, "0");
    private static final Field DB_PORT = new IntegerField("port", "DB Port", "5432", false, false, "1");
    private static final Field DB_USER = new NonBlankField("username", "Username", null, true, false, "2");
    private static final Field DB_PASS = new Field("password", "User Password", null, true, true, "3");
    private static final Field DB_NAME = new NonBlankField("name", "DB Name", null, true, false, "4");
    private static final Field SSL_MODE = new Field("ssl_mode", "SSL verification level", "verify-full", false, false, "6");
    private static final Field DB_CONNECTION = new Field("connection", "DB Connection", null, false, false, "11");
    private static final Field MAX_CONNECTIONS_ACTIVE = new IntegerField("max_connections_active", "Maximum Active Connections", null, false, false, "12");
    private static final Field MAX_CONNECTIONS_IDLE = new IntegerField("max_connections_idle", "Maximum Idle Connections", null, false, false, "13");
    private static final Field MAX_CONNECTION_WAIT_TIME = new IntegerField("max_connection_wait_time", "Maximum connection wait time", null, false, false, "14");
    private static final Field PERIODIC_CLEANUP_TIME = new TimeField("periodic_cleanup_time", "Periodic Cleanup Time", null, false, false, "15");
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    static {
        FIELDS.put(DB_HOST.key(), DB_HOST);
        FIELDS.put(DB_PORT.key(), DB_PORT);
        FIELDS.put(DB_USER.key(), DB_USER);
        FIELDS.put(DB_NAME.key(), DB_NAME);
        FIELDS.put(DB_PASS.key(), DB_PASS);
        FIELDS.put(USE_SSL.key(), USE_SSL);
        FIELDS.put(SSL_MODE.key(), SSL_MODE);
        FIELDS.put(ROOT_CERT.key(), ROOT_CERT);
        FIELDS.put(CLIENT_CERT.key(), CLIENT_CERT);
        FIELDS.put(CLIENT_KEY.key(), CLIENT_KEY);
        FIELDS.put(CLIENT_PKCS8_KEY.key(), CLIENT_PKCS8_KEY);
        FIELDS.put(DB_CONNECTION.key(), DB_CONNECTION);
        FIELDS.put(MAX_CONNECTIONS_ACTIVE.key(), MAX_CONNECTIONS_ACTIVE);
        FIELDS.put(MAX_CONNECTIONS_IDLE.key(), MAX_CONNECTIONS_IDLE);
        FIELDS.put(MAX_CONNECTION_WAIT_TIME.key(), MAX_CONNECTION_WAIT_TIME);
        FIELDS.put(PERIODIC_CLEANUP_TIME.key(), PERIODIC_CLEANUP_TIME);
    }

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}
