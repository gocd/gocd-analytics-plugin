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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.executors.RequestFromServer;
import com.thoughtworks.gocd.analytics.executors.StageStatusRequestExecutor;
import com.thoughtworks.gocd.analytics.executors.notification.AgentStatusRequestExecutor;
import com.thoughtworks.gocd.analytics.executors.notification.NotificationInterestedInExecutor;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.utils.Util;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.BAD_REQUEST;

@Extension
public class NotificationPlugin implements GoPlugin, Initializable {
    public static final Logger LOG = Logger.getLoggerFor(NotificationPlugin.class);
    private GoApplicationAccessor accessor;

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        LOG.debug("Received plugin request from server for message - {}", request.requestName());
        try {
            switch (RequestFromServer.fromString(request.requestName())) {
                case REQUEST_NOTIFICATIONS_INTERESTED_IN:
                    return new NotificationInterestedInExecutor().execute();
                case REQUEST_STAGE_STATUS:
                    return new StageStatusRequestExecutor(request, Bootstrap.instance().sessionFactory(this), healthService()).execute();
                case REQUEST_AGENT_STATUS:
                    return new AgentStatusRequestExecutor(request, Bootstrap.instance().sessionFactory(this), healthService()).execute();
                case PLUGIN_SETTINGS_GET_VIEW:
                    return new DefaultGoPluginApiResponse(BAD_REQUEST, "");
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new DefaultGoPluginApiResponse(BAD_REQUEST, "");
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Error while executing request: {}", request.requestName(), e);
            return DefaultGoPluginApiResponse.error("Failed to handle request " + request.requestName() + " due to:" + e.getMessage());
        }
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
        healthService().initializeNotifier(accessor);
    }

    @Load
    public void onLoad(PluginContext ctx) {
        LOG.info("Loading plugin " + Util.pluginId() + " version " + Util.fullVersion());
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PluginConstants.NOTIFICATION_PLUGIN_IDENTIFIER;
    }

    @Override
    public PluginSettingRequestProcessor request() {
        return new PluginSettingRequestProcessor(accessor);
    }

    @Override
    public PluginHealthMessageService healthService() {
        return PluginHealthMessageService.instance();
    }
}
