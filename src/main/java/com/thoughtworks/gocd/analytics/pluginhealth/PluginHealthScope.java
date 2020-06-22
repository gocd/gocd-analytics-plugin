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


import java.util.Objects;

public class PluginHealthScope {
    private final ScopeType scopeType;
    private final String scope;

    public PluginHealthScope(ScopeType type, String scope) {
        this.scopeType = type;
        this.scope = scope;
    }

    public static PluginHealthScope forPluginSettings() {
        return new PluginHealthScope(ScopeType.PLUGIN_SETTINGS, "global");
    }

    public static PluginHealthScope forAgentStatusNotification() {
        return new PluginHealthScope(ScopeType.AGENT_STATUS_NOTIFICATION, "global");
    }

    public static PluginHealthScope forStageStatusNotification() {
        return new PluginHealthScope(ScopeType.STAGE_STATUS_NOTIFICATION, "global");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginHealthScope)) return false;
        PluginHealthScope that = (PluginHealthScope) o;
        return scopeType == that.scopeType &&
                Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeType, scope);
    }

    enum ScopeType {
        PLUGIN_SETTINGS,
        STAGE_STATUS_NOTIFICATION,
        AGENT_STATUS_NOTIFICATION
    }
}
