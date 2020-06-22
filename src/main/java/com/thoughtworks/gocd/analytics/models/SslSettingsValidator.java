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


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.executors.GetPluginConfigurationExecutor.*;

public class SslSettingsValidator {

    public List<Map<String, String>> validate(ValidatePluginSettings settings) {
        List<Map<String, String>> errorResult = new ArrayList<>();

        if (settings.get(USE_SSL.key()) != null && settings.get(USE_SSL.key()).equals("true")) {
            validate(ROOT_CERT.key(), settings.get(ROOT_CERT.key()), ROOT_CERT.displayName, errorResult);
            validate(CLIENT_CERT.key(), settings.get(CLIENT_CERT.key()), CLIENT_CERT.displayName, errorResult);
            validate(CLIENT_KEY.key(), settings.get(CLIENT_KEY.key()), CLIENT_KEY.displayName, errorResult);
            validate(CLIENT_PKCS8_KEY.key(), settings.get(CLIENT_PKCS8_KEY.key()), CLIENT_PKCS8_KEY.displayName, errorResult);
        }

        return errorResult;
    }

    private void validate(String key, String value, String displayName, List<Map<String, String>> errorResult) {
        if (StringUtils.isBlank(value)) {
            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("message", displayName + " must be provided if SSL is enabled.");
            errorResult.add(result);
        }
    }
}
