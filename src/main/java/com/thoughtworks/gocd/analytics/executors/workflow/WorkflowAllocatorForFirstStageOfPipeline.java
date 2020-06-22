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

import com.thoughtworks.gocd.analytics.dao.*;
import com.thoughtworks.gocd.analytics.models.*;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class WorkflowAllocatorForFirstStageOfPipeline extends WorkflowAllocator {
    public WorkflowAllocatorForFirstStageOfPipeline() {
        this(new PipelineDAO(), new StageDAO(), new MaterialRevisionDAO(), new WorkflowDAO(), new PipelineWorkflowDAO());
    }

    protected WorkflowAllocatorForFirstStageOfPipeline(PipelineDAO pipelineDAO, StageDAO stageDAO, MaterialRevisionDAO materialRevisionDAO,
                                                       WorkflowDAO workflowDAO, PipelineWorkflowDAO pipelineWorkflowDAO) {
        super(pipelineDAO, stageDAO, materialRevisionDAO, workflowDAO, pipelineWorkflowDAO);
    }

    @Override
    public void allocate(SqlSession sqlSession, PipelineInstance pipelineInstance, Stage stage, List<MaterialRevision> materialRevisions) {
        List<Long> scmMaterialsWorkflows = workflowsForSCMMaterials(sqlSession, pipelineInstance, scmMaterialRevisions(materialRevisions));
        List<Long> dependencyMaterialsWorkflows = workflowsForDependencyMaterials(sqlSession, materialRevisions);

        List<Long> workflowIds = Stream.concat(scmMaterialsWorkflows.stream(), dependencyMaterialsWorkflows.stream()).distinct().collect(Collectors.toList());

        LOG.debug("[Workflow-Allocator] Associating stage: `{}` to workflow ids: `{}`", stage.toShortString(), workflowIds(workflowIds));

        workflowIds.forEach(id -> pipelineWorkflowDAO.insert(sqlSession, pipelineInstance.getId(), stage.getId(), id));
    }

    private List<Long> workflowsForDependencyMaterials(SqlSession sqlSession, List<MaterialRevision> materialRevisions) {
        List<Long> workflowIds = new ArrayList<>();
        materialRevisions.stream()
                .filter(m -> !m.isSCMMaterial())
                .forEach(materialRevision -> {
                    DependencyMaterialRevision revision = new DependencyMaterialRevision(materialRevision.getRevision());

                    PipelineInstance pipeline = pipelineDAO.find(sqlSession, revision.pipelineName, revision.pipelineCounter);
                    Stage stage = stageDAO.find(sqlSession, revision.pipelineName, revision.pipelineCounter, revision.stageName, revision.stageCounter);

                    if (pipeline == null || stage == null) return;

                    List<Long> ids = pipelineWorkflowDAO.workflowsFor(sqlSession, pipeline.getId(), stage.getId()).stream().map(PipelineWorkflow::getWorkflowId).collect(Collectors.toList());
                    workflowIds.addAll(ids);
                });

        return workflowIds;
    }

    private List<Long> workflowsForSCMMaterials(SqlSession sqlSession, PipelineInstance pipelineInstance, List<MaterialRevision> scmMaterials) {
        return scmMaterials.stream().map(materialRevision -> {
            PipelineWorkflow workflowForMaterial = pipelineWorkflowDAO.workflowFor(sqlSession, materialRevision.getId());

            if (workflowForMaterial == null) {
                Workflow workflow = new Workflow(pipelineInstance.getCreatedAt());
                workflowDAO.insert(sqlSession, workflow);
                pipelineWorkflowDAO.insert(sqlSession, materialRevision.getId(), workflow.getId());

                return workflow.getId();
            }

            return workflowForMaterial.getWorkflowId();
        }).collect(toList());
    }

    private class DependencyMaterialRevision {
        private final String pipelineName;
        private final int pipelineCounter;
        private final String stageName;
        private final int stageCounter;

        public DependencyMaterialRevision(String revision) {
            String[] s = revision.split("/");
            pipelineName = s[0];
            pipelineCounter = Integer.parseInt(s[1]);
            stageName = s[2];
            stageCounter = Integer.parseInt(s[3]);
        }
    }

    private String workflowIds(List<Long> workflowIds) {
        return workflowIds.stream().map(w -> Long.toString(w)).collect(Collectors.joining(","));
    }
}
