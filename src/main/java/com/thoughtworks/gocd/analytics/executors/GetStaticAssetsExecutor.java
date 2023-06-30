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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static com.thoughtworks.gocd.analytics.utils.Util.readResourceBytes;

public class GetStaticAssetsExecutor implements RequestExecutor {
    private static final String KEY = "assets";

    private String resourcePath;

    public GetStaticAssetsExecutor(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE);
        response.setResponseBody(
                GSON.toJson(Collections.singletonMap(KEY, new String(
                        Base64.getEncoder().encode(readResourceBytes(resourcePath)),
                        StandardCharsets.ISO_8859_1
                )))
        );
        return response;
    }
}
