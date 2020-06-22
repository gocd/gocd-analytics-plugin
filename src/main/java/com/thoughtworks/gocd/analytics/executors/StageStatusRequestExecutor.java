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

package com.thoughtworks.gocd.analytics.executors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.MaterialRevisionDAO;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.db.TransactionAware;
import com.thoughtworks.gocd.analytics.executors.workflow.WorkflowAllocatorFactory;
import com.thoughtworks.gocd.analytics.models.*;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessage;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthScope;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthState;
import com.thoughtworks.gocd.analytics.utils.Builder;
import org.apache.ibatis.session.SqlSession;

import java.util.Collections;
import java.util.List;

import static java.text.MessageFormat.format;

public class StageStatusRequestExecutor extends TransactionAware implements RequestExecutor {
    public static final Logger LOG = Logger.getLoggerFor(StageStatusRequestExecutor.class);
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private final StageStatusRequest request;
    private final PluginHealthMessageService pluginHealthMessageService;
    private final MaterialRevisionDAO materialRevisionDAO;
    private final WorkflowAllocatorFactory workflowAllocatorFactory;
    private JobDAO jobDAO;
    private StageDAO stageDAO;
    private Builder builder;
    private PipelineDAO pipelineDAO;

    public StageStatusRequestExecutor(GoPluginApiRequest request, SessionFactory sessionFactory,
                                      PluginHealthMessageService pluginHealthMessageService) {
        this(request, new JobDAO(), new StageDAO(), new PipelineDAO(), new MaterialRevisionDAO(),
                new Builder(), sessionFactory, pluginHealthMessageService, new WorkflowAllocatorFactory());
    }

    protected StageStatusRequestExecutor(GoPluginApiRequest request, JobDAO jobDAO, StageDAO stageDAO,
                                         PipelineDAO pipelineDAO, MaterialRevisionDAO materialRevisionDAO,
                                         Builder builder, SessionFactory sessionFactory,
                                         PluginHealthMessageService pluginHealthMessageService,
                                         WorkflowAllocatorFactory workflowAllocatorFactory) {
        super(sessionFactory);
        this.workflowAllocatorFactory = workflowAllocatorFactory;
        LOG.debug("[Stage-Status] Received request: {}", request.requestBody());
        this.pluginHealthMessageService = pluginHealthMessageService;
        this.request = StageStatusRequest.fromJSON(request.requestBody());
        this.jobDAO = jobDAO;
        this.stageDAO = stageDAO;
        this.pipelineDAO = pipelineDAO;
        this.materialRevisionDAO = materialRevisionDAO;
        this.builder = builder;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        if (cannotConnectToDB()) {
            PluginHealthMessage message = PluginHealthMessage.warn("No active connection to PostgresDb, cannot store stage status information.");
            pluginHealthMessageService.update(new PluginHealthState(message, PluginHealthScope.forPluginSettings()));

            return successResponse();
        }

        if (isStageCompleted()) {
            try {
                pluginHealthMessageService.removeByScope(PluginHealthScope.forStageStatusNotification());
                PipelineInstance pipelineInstance = builder.buildPipelineInstance(this.request.pipeline);
                List<MaterialRevision> materialRevisions = builder.materialRevisionsResponsibleForTheBuild(this.request.pipeline);
                Stage stage = builder.buildStage(this.request.pipeline);
                List<Job> jobs = builder.buildJobs(this.request.pipeline);

                doInTransaction(new Operation<Boolean>() {
                    @Override
                    public Boolean execute(SqlSession sqlSession) {
                        insertSCMMaterialRevisions(sqlSession, materialRevisions);
                        updateOrInsertPipelineInstance(sqlSession, pipelineInstance);
                        insertStage(sqlSession, stage);
                        insertJobs(sqlSession, jobs);
                        workflowAllocatorFactory.allocatorFor(stage).allocate(sqlSession, pipelineInstance, stage, materialRevisions);
                        return true;
                    }
                });
            } catch (Exception e) {
                LOG.error("[Stage-Status] Error storing Stage status data.", e);
                PluginHealthMessage error = PluginHealthMessage.error(format("Error storing Stage status data, reason: {0}", e.getMessage()));
                pluginHealthMessageService.update(new PluginHealthState(error, PluginHealthScope.forStageStatusNotification()));
            }
        }

        return successResponse();
    }

    private void updateOrInsertPipelineInstance(SqlSession sqlSession, PipelineInstance pipelineInstance) {
        accountForReRuns(sqlSession, pipelineInstance, this.request.pipeline.stage);
        pipelineDAO.updateOrInsert(sqlSession, pipelineInstance);
    }

    private boolean cannotConnectToDB() {
        return sessionFactory == null;
    }

    private void accountForReRuns(SqlSession sqlSession, PipelineInstance pipelineInstance, StageStatusRequest.Stage stage) {
        if (stage.isReRun()) {
            Stage previousStageRun = stageDAO.One(sqlSession, pipelineInstance, stage.name, Integer.valueOf(stage.counter) - 1);
            if (previousStageRun != null) {
                pipelineInstance.setTotalTimeSecs(pipelineInstance.getTotalTimeSecs() - previousStageRun.getTotalTimeSecs());
                pipelineInstance.setTimeWaitingSecs(pipelineInstance.getTimeWaitingSecs() - previousStageRun.getTimeWaitingSecs());
            }
        }
    }

    private void insertStage(SqlSession sqlSession, Stage stage) {
        stageDAO.insert(sqlSession, stage);
    }

    private boolean isStageCompleted() {
        return this.request.pipeline.stage.isCompleted();
    }

    private void insertJobs(SqlSession sqlSession, List<Job> jobs) {
        if (!jobs.isEmpty()) {
            jobDAO.insertJobs(sqlSession, jobs);
        }
    }

    private void insertSCMMaterialRevisions(SqlSession sqlSession, List<MaterialRevision> materialRevisions) {
        materialRevisions.stream()
                .filter(MaterialRevision::isSCMMaterial)
                .forEach(m -> materialRevisionDAO.insert(sqlSession, m));
    }

    private GoPluginApiResponse successResponse() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(Collections.<String, Object>singletonMap("status", "success")));
    }
}
