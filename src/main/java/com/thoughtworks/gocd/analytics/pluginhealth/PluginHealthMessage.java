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

package com.thoughtworks.gocd.analytics.pluginhealth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class PluginHealthMessage {
    @Expose
    @SerializedName("type")
    String type;

    @Expose
    @SerializedName("message")
    String message;

    public PluginHealthMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public static PluginHealthMessage error(String message) {
        return new PluginHealthMessage("error", message);
    }

    public static PluginHealthMessage warn(String message) {
        return new PluginHealthMessage("warning", message);
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginHealthMessage)) return false;
        PluginHealthMessage that = (PluginHealthMessage) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getMessage());
    }
}
