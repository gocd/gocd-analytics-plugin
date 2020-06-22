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

package com.thoughtworks.gocd.analytics.executors.workflow;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.dao.*;
import com.thoughtworks.gocd.analytics.models.MaterialRevision;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.stream.Collectors;

public abstract class WorkflowAllocator {
    protected static final Logger LOG = Logger.getLoggerFor(WorkflowAllocator.class);
    protected final PipelineDAO pipelineDAO;
    protected final StageDAO stageDAO;
    protected final MaterialRevisionDAO materialRevisionDAO;
    protected final WorkflowDAO workflowDAO;
    protected final PipelineWorkflowDAO pipelineWorkflowDAO;

    protected WorkflowAllocator(PipelineDAO pipelineDAO, StageDAO stageDAO, MaterialRevisionDAO materialRevisionDAO,
                                WorkflowDAO workflowDAO, PipelineWorkflowDAO pipelineWorkflowDAO) {

        this.pipelineDAO = pipelineDAO;
        this.stageDAO = stageDAO;
        this.materialRevisionDAO = materialRevisionDAO;
        this.workflowDAO = workflowDAO;
        this.pipelineWorkflowDAO = pipelineWorkflowDAO;
    }

    public abstract void allocate(SqlSession sqlSession, PipelineInstance pipelineInstance, Stage stage, List<MaterialRevision> materialRevisions);

    protected List<MaterialRevision> scmMaterialRevisions(List<MaterialRevision> materialRevisions) {
        return materialRevisions.stream()
                .filter(MaterialRevision::isSCMMaterial)
                .collect(Collectors.toList());
    }
}
