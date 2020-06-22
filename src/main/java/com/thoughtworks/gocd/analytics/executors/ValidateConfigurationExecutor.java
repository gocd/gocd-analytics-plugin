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
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.TestConnectionResult;
import com.thoughtworks.gocd.analytics.db.Database;
import com.thoughtworks.gocd.analytics.db.PostgresqlDatabase;
import com.thoughtworks.gocd.analytics.models.Field;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.models.SslSettingsValidator;
import com.thoughtworks.gocd.analytics.models.ValidatePluginSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidateConfigurationExecutor implements RequestExecutor {
    private static final Gson GSON = new Gson();
    private final Database database;
    private PluginSettings pluginSettings;
    private ValidatePluginSettings validatePluginSettings;

    public ValidateConfigurationExecutor(GoPluginApiRequest request) {
        this(request, new PostgresqlDatabase());
    }

    protected ValidateConfigurationExecutor(GoPluginApiRequest request, Database database) {
        pluginSettings = PluginSettings.fromValidateSettingsJSON(request.requestBody());
        validatePluginSettings = ValidatePluginSettings.fromJSON(request.requestBody());
        this.database = database;
    }

    public GoPluginApiResponse execute() {
        List<Map<String, String>> result = new ArrayList<>();

        for (Map.Entry<String, Field> entry : GetPluginConfigurationExecutor.FIELDS.entrySet()) {
            Field field = entry.getValue();
            Map<String, String> validationError = field.validate(validatePluginSettings.get(entry.getKey()));

            if (!validationError.isEmpty()) {
                result.add(validationError);
            }
        }

        result.addAll(new SslSettingsValidator().validate(validatePluginSettings));

        if (result.isEmpty()) {
            TestConnectionResult connectionResult = this.database.testConnection(pluginSettings);
            if (!connectionResult.isSuccessful()) {
                Map<String, String> dbError = new HashMap<>();
                dbError.put("key", "connection");
                dbError.put("message", connectionResult.errorMessage());
                result.add(dbError);
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(result));
    }

}
