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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginHealthMessageService {
    private final static PluginHealthMessageService pluginHealthMessageService = new PluginHealthMessageService();
    private Map<PluginHealthState, PluginHealthMessage> messages = new ConcurrentHashMap<>();
    private PluginHealthMessageNotifier pluginHealthMessageNotifier;

    private PluginHealthMessageService() {
    }

    protected PluginHealthMessageService(PluginHealthMessageNotifier pluginHealthMessageNotifier) {
        this.pluginHealthMessageNotifier = pluginHealthMessageNotifier;
    }

    public static PluginHealthMessageService instance() {
        return pluginHealthMessageService;
    }

    public void update(PluginHealthState pluginHealthState) {
        messages.put(pluginHealthState, pluginHealthState.getPluginHealthMessage());
        pluginHealthMessageNotifier.notify(messages.values());
    }

    public void remove(PluginHealthState pluginHealthState) {
        messages.remove(pluginHealthState);
        pluginHealthMessageNotifier.notify(messages.values());
    }

    public void removeByScope(PluginHealthScope pluginHealthScope) {
        messages.keySet().stream().forEach(s -> {
            if (pluginHealthScope.equals(s.getPluginHealthScope())) {
                messages.remove(s);
            }
        });
        pluginHealthMessageNotifier.notify(messages.values());
    }

    public Collection<PluginHealthMessage> all() {
        return messages.values();
    }

    public void initializeNotifier(GoApplicationAccessor accessor) {
        if (pluginHealthMessageNotifier == null && accessor != null) {
            this.pluginHealthMessageNotifier = new PluginHealthMessageNotifier(accessor);
        }
    }
}
