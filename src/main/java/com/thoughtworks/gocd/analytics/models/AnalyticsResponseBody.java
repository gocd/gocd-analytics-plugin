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
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;

import java.time.ZonedDateTime;

public class AnalyticsResponseBody {
    static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ZonedDateTime.class, new DefaultZonedDateTimeTypeAdapter())
            .create();
    @Expose
    @SerializedName("data")
    private String data;

    @Expose
    @SerializedName("view_path")
    private String viewPath;

    public AnalyticsResponseBody(Object data, String viewPath) {
        this.data = GSON.toJson(data);
        this.viewPath = viewPath;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public String getData() {
        return data;
    }

    public String getViewPath() {
        return viewPath;
    }
}
