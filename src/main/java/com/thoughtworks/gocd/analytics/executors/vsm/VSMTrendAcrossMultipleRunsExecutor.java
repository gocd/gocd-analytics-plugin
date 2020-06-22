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

package com.thoughtworks.gocd.analytics.executors.vsm;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.VSMGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thoughtworks.gocd.analytics.utils.ResponseMessages.infoMessage;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class VSMTrendAcrossMultipleRunsExecutor extends AbstractSessionFactoryAwareExecutor {
    public static final Logger LOG = Logger.getLoggerFor(VSMTrendAcrossMultipleRunsExecutor.class);
    private PipelineDAO pipelineDAO;

    public VSMTrendAcrossMultipleRunsExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, sessionFactory, new PipelineDAO());
    }

    VSMTrendAcrossMultipleRunsExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory, PipelineDAO pipelineDAO) {
        super(analyticsRequest, sessionFactory);
        this.pipelineDAO = pipelineDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(param(PARAM_VSM_GRAPH));
        LOG.debug("[VSM-Trends-Chart] Received request with Source: '{}', Destination: '{}' and VSM Graph: '{}'",
                param(PARAM_VSM_SOURCE), param(PARAM_VSM_DESTINATION), param(PARAM_VSM_GRAPH));

        List<String> pipelinesInWorkflow = vsmGraph.pipelinesInWorkflow(param(PARAM_VSM_SOURCE), param(PARAM_VSM_DESTINATION));
        LOG.debug("[VSM-Trends-Chart] Pipelines in workflow from Source: '{}' to Destination: '{}' : '[{}]'",
                param(PARAM_VSM_SOURCE), param(PARAM_VSM_DESTINATION), String.join(", ", pipelinesInWorkflow));

        List<PipelineInstance> pipelineInstances = doInTransaction(sqlSession -> pipelineDAO.allPipelineInstancesWithNameIn(sqlSession,
                pipelinesInWorkflow.get(0), pipelinesInWorkflow));

        if(pipelineInstances == null || pipelineInstances.isEmpty()) {
            return infoMessage(null);
        }

        Map<Long, List<PipelineInstance>> pipelinesGroupedByWorkflow = groupByWorkflow(pipelineInstances, pipelinesInWorkflow);

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(response(pipelinesGroupedByWorkflow, pipelinesInWorkflow), viewPath());

        return new DefaultGoPluginApiResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, responseBody.toJson());
    }

    private Map<Long, List<PipelineInstance>> groupByWorkflow(List<PipelineInstance> pipelineInstances, List<String> pipelineNames) {
        return pipelineInstances.stream().collect(
                Collectors.groupingBy(PipelineInstance::getWorkflowId,
                        collectingAndThen(toCollection(ArrayList::new), (List<PipelineInstance> l) -> {
                            l.sort(Comparator.comparingInt(o -> pipelineNames.indexOf(o.getName())));
                            return l;
                        })));
    }

    private Map<String, Object> response(Map<Long, List<PipelineInstance>> pipelinesGroupedByWorkflow, List<String> pipelinesInWorkflow) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("workflows", pipelinesGroupedByWorkflow),
                new AbstractMap.SimpleEntry<>("pipelines", pipelinesInWorkflow)
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    }

    private String viewPath() {
        return "workflow-trends-chart.html";
    }
}
