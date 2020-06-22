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

package com.thoughtworks.gocd.analytics.utils;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;

import java.util.Collections;
import java.util.Map;

public class ResponseMessages {
    public static GoPluginApiResponse infoMessage(String message) {
        return flashResponse("info-message.html", message != null ? Collections.singletonMap("msg", message) : null);
    }

    public static GoPluginApiResponse errorMessage(String message) {
        return flashResponse("error-message.html", message != null ? Collections.singletonMap("msg", message) : null);
    }

    private static GoPluginApiResponse flashResponse(String path, Map<String, String> params) {
        if (null != params && params.size() > 0) {
            boolean firstKey = true;
            for (String key : params.keySet()) {
                String param = params.get(key);
                path += (firstKey ? "?" : "&") + key + "=" + ((null != param) ? ViewUtils.jsUriEncode(param) : "");
                firstKey = false;
            }
        }
        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
                new AnalyticsResponseBody(Collections.EMPTY_MAP, path).toJson());
    }
}
