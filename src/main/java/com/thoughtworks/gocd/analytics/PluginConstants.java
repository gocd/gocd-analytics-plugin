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

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;

import java.util.Collections;

public interface PluginConstants {
    // The extension point API version that this plugin understands
    String ANALYTICS_API_VERSION = "2.0";
    String PLUGIN_SETTINGS_API_VERSION = "1.0";
    String NOTIFICATION_API_VERSION = "4.0";

    // the identifier of this plugin
    GoPluginIdentifier ANALYTICS_PLUGIN_IDENTIFIER = new GoPluginIdentifier("analytics", Collections.singletonList(ANALYTICS_API_VERSION));
    GoPluginIdentifier NOTIFICATION_PLUGIN_IDENTIFIER = new GoPluginIdentifier("notification", Collections.singletonList(NOTIFICATION_API_VERSION));

    // requests that the plugin makes to the server
    String REQUEST_SERVER_PREFIX = "go.processor";
    String REQUEST_SERVER_GET_PLUGIN_SETTINGS = REQUEST_SERVER_PREFIX + ".plugin-settings.get";
}
