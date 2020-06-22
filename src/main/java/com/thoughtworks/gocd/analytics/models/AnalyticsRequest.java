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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AnalyticsRequest {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("type")
    private String type;

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("params")
    private Map<String, String> params;

    public AnalyticsRequest() {
    }

    public AnalyticsRequest(String type, String id, Map<String, String> params) {
        this.id = id;
        this.type = type;
        this.params = params;
    }

    public static AnalyticsRequest fromJSON(String json) {
        return GSON.fromJson(json, AnalyticsRequest.class);
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "AnalyticsRequest{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", params=" + params +
                '}';
    }
}
