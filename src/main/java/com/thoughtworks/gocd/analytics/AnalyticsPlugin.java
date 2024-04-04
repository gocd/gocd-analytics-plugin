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
import com.thoughtworks.go.plugin.api.annotation.UnLoad;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.executors.*;
import com.thoughtworks.gocd.analytics.executors.agent.AgentStateTransitionExecutor;
import com.thoughtworks.gocd.analytics.executors.agent.AgentUtilizationExecutor;
import com.thoughtworks.gocd.analytics.executors.agent.AgentsWithHighestUtilizationExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobBuildTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobBuildTimeOnAgentExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobTimelineExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobsHighestWaitTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobsHighestWaitTimeOnAgentExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobsWaitVsAgentsAvailableExecutor;
import com.thoughtworks.gocd.analytics.executors.job.PriorityJobDetailsExecutor;
import com.thoughtworks.gocd.analytics.executors.job.PriorityJobExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.ActualPipelineRuntimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelineBuildTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelineRuntimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelineStateSummaryExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelinesHighestWaitTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PriorityExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PriorityPipelineDetailsExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PriorityPipelineExecutor;
import com.thoughtworks.gocd.analytics.executors.stage.PriorityStageDetailsExecutor;
import com.thoughtworks.gocd.analytics.executors.stage.PriorityStageExecutor;
import com.thoughtworks.gocd.analytics.executors.stage.StageBuildTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.stage.StageTimelineExecutor;
import com.thoughtworks.gocd.analytics.executors.vsm.VSMTrendAcrossMultipleRunsExecutor;
import com.thoughtworks.gocd.analytics.executors.vsm.VSMWorkflowTimeDistributionExecutor;
import com.thoughtworks.gocd.analytics.models.AgentUtilizationSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummary;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.utils.Util;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.INTERNAL_ERROR;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
import static com.thoughtworks.gocd.analytics.AvailableAnalytics.*;

@Extension
public class AnalyticsPlugin implements GoPlugin, Initializable {

    public static final Logger LOG = Logger.getLoggerFor(AnalyticsPlugin.class);
    private static final String ASSETS_RESOURCE = "/assets.zip";

    private GoApplicationAccessor accessor;

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request)
        throws UnhandledRequestTypeException {
        LOG.debug("Received plugin request from server for message - {}", request.requestName());
        try {
            switch (RequestFromServer.fromString(request.requestName())) {
                case REQUEST_GET_PLUGIN_ICON:
                    return new GetPluginIconExecutor().execute();
                case REQUEST_GET_CAPABILITIES:
                    return new GetCapabilitiesExecutor().execute();
                case REQUEST_GET_ANALYTICS:
                    Bootstrap bootstrap = Bootstrap.instance();
                    return AnalyticsExecutorSelector.instance()
                        .executorFor(request, bootstrap.sessionFactory(this)).execute();
                case REQUEST_GET_STATIC_ASSETS:
                    return new GetStaticAssetsExecutor(ASSETS_RESOURCE).execute();
                case PLUGIN_SETTINGS_GET_VIEW:
                    return new GetViewRequestExecutor().execute();
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                    return new GetPluginConfigurationExecutor().execute();
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return new ValidateConfigurationExecutor(request).execute();
                case PLUGIN_SETTINGS_CHANGE_NOTIFICATION:
                    reinitializePlugin(PluginSettings.fromJSON(request.requestBody()));
                    return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, "");
                default:
                    throw new UnhandledRequestTypeException(request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Error while executing request " + request.requestName(), e);
            return new DefaultGoPluginApiResponse(INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        LOG.info("Initializing plugin...");
        preInit(accessor);

        LOG.debug("Registering analytics executors.");
        AnalyticsExecutorSelector.instance()
            .registerExecutor(PIPELINE_BUILD_TIME.getId(), PipelineBuildTimeExecutor.class)
            .registerExecutor(JOBS_WITH_THE_HIGHEST_WAIT_TIME.getId(),
                JobsHighestWaitTimeExecutor.class)
            .registerExecutor(JOBS_WITH_THE_HIGHEST_WAIT_TIME_ON_AGENT.getId(),
                JobsHighestWaitTimeOnAgentExecutor.class)
            .registerExecutor(PIPELINES_WITH_THE_HIGHEST_WAIT_TIME.getId(),
                PipelinesHighestWaitTimeExecutor.class)
            .registerExecutor(JOB_BUILD_TIME.getId(), JobBuildTimeExecutor.class)
            .registerExecutor(AGENTS_WITH_THE_HIGHEST_UTILIZATION.getId(),
                AgentsWithHighestUtilizationExecutor.class)
            .registerExecutor(JOB_BUILD_TIME_ON_AGENT.getId(), JobBuildTimeOnAgentExecutor.class)
            .registerExecutor(STAGE_BUILD_TIME.getId(), StageBuildTimeExecutor.class)
            .registerExecutor(AGENT_STATE_TRANSITION.getId(), AgentStateTransitionExecutor.class)
            .registerExecutor(VSM_TREND_ACROSS_MULTIPLE_RUNS.getId(),
                VSMTrendAcrossMultipleRunsExecutor.class)
            .registerExecutor(VSM_WORKFLOW_TIME_DISTRIBUTION.getId(),
                VSMWorkflowTimeDistributionExecutor.class)
            .registerExecutor(PIPELINES_RUNTIME_ACROSS_TIMELINE.getId(),
                PipelineRuntimeExecutor.class)
            .registerExecutor(PIPELINE_TIMELINE.getId(), ActualPipelineRuntimeExecutor.class)
            .registerExecutor(STAGE_TIMELINE.getId(), StageTimelineExecutor.class)
            .registerExecutor(JOB_TIMELINE.getId(), JobTimelineExecutor.class)
            .registerExecutor(PRIORITY.getId(), PriorityExecutor.class)
            .registerExecutor(PRIORITY_PIPELINE.getId(), PriorityPipelineExecutor.class)
            .registerExecutor(PRIORITY_STAGE.getId(), PriorityStageExecutor.class)
            .registerExecutor(PRIORITY_JOB.getId(), PriorityJobExecutor.class)
            .registerExecutor(PRIORITY_PIPELINE_DETAILS.getId(), PriorityPipelineDetailsExecutor.class)
            .registerExecutor(PRIORITY_STAGE_DETAILS.getId(), PriorityStageDetailsExecutor.class)
            .registerExecutor(PRIORITY_JOB_DETAILS.getId(), PriorityJobDetailsExecutor.class)
            .registerExecutor(PIPELINE_STATE_SUMMARY.getId(), PipelineStateSummaryExecutor.class)
            .registerExecutor(JOBS_WAIT_VS_AGENTS_AVAILABLE.getId(), JobsWaitVsAgentsAvailableExecutor.class)
            .registerExecutor(HELPER_AGENT_UTILIZATION.getId(), AgentUtilizationExecutor.class)
        ;

        LOG.info("Initialized.");
    }

    private void reinitializePlugin(PluginSettings pluginSettings) {
        LOG.info("Reinitializing plugin on plugin settings update...");

        try {
            Bootstrap.instance().ensure(this, pluginSettings);
            LOG.info("Done reinitializing plugin.");
        } catch (Exception e) {
            LOG.error("Failed to reinitialize plugin.", e);
        }
    }

    @Load
    public void onLoad(PluginContext ctx) {
        LOG.info("Loading plugin " + Util.pluginId() + " version " + Util.fullVersion());
    }

    @UnLoad
    public void unLoad(PluginContext ctx) {
        LOG.info("Unloading plugin.");
        try {
            Bootstrap.instance().teardown();
        } catch (Exception e) {
            LOG.error("Error unloading plugin.", e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PluginConstants.ANALYTICS_PLUGIN_IDENTIFIER;
    }

    private void preInit(GoApplicationAccessor accessor) {
        this.accessor = accessor;
        healthService().initializeNotifier(accessor);
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
