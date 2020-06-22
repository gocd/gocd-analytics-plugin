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


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.analytics.AvailableAnalytics;

public class SupportedAnalytics {
    @Expose
    @SerializedName("type")
    private String type;

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("title")
    private String title;

    public SupportedAnalytics(AvailableAnalytics availableAnalytics) {
        this.type = availableAnalytics.getType();
        this.id = availableAnalytics.getId();
        this.title = availableAnalytics.getTitle();
    }
}
