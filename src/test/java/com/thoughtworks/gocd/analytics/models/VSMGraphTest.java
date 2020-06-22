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

package com.thoughtworks.gocd.analytics.models;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class VSMGraphTest {
    @Test
    public void shouldSerializeFromJSON() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(simpleGraph);

        assertThat(vsmGraph.currentPipeline, is("P4"));
        assertThat(vsmGraph.levels.size(), is(2));

        assertThat(vsmGraph.levels.get(0).nodes.size(), is(1));
        VSMGraph.MaterialNode materialNode = (VSMGraph.MaterialNode) vsmGraph.levels.get(0).nodes.get(0);

        assertThat(materialNode.id, is("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629"));
        assertThat(materialNode.dependents, containsInAnyOrder("P1", "P2", "P4"));
        assertThat(materialNode.parents.size(), is(0));
        assertThat(materialNode.type, is("GIT"));
        assertThat(materialNode.materialRevisions.size(), is(1));
        assertThat(materialNode.materialRevisions.get(0).modifications.size(), is(1));
        assertThat(materialNode.materialRevisions.get(0).modifications.get(0).revision, is("2a13c2e8cf1661d905099e7297dba3c5b58bce7c"));

        assertThat(vsmGraph.levels.get(1).nodes.size(), is(1));
        VSMGraph.PipelineNode pipelineNode = (VSMGraph.PipelineNode) vsmGraph.levels.get(1).nodes.get(0);

        assertThat(pipelineNode.id, is("P1"));
        assertThat(pipelineNode.parents, contains("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629"));
        assertThat(pipelineNode.dependents, contains("P3"));
        assertThat(pipelineNode.type, is("PIPELINE"));
        assertThat(pipelineNode.instances.size(), is(1));
        assertThat(pipelineNode.instances.get(0).counter, is(1));
        assertThat(pipelineNode.instances.get(0).label, is("1"));
        assertThat(pipelineNode.instances.get(0).label, is("1"));
        assertThat(pipelineNode.instances.get(0).stages.size(), is(1));
        assertThat(pipelineNode.instances.get(0).stages.get(0).name, is("defaultStage"));
        assertThat(pipelineNode.instances.get(0).stages.get(0).duration, is(30));
        assertThat(pipelineNode.instances.get(0).stages.get(0).status, is("Passed"));
        assertThat(pipelineNode.instances.get(0).stages.get(0).counter, is(1));
    }

    @Test
    public void shouldListAllPipelinesBetweenASourceMaterialAndDestinationPipelineExcludingTheSourceMaterial() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(complexGraphWithFanInAndFanOut);

        List<String> pipelines = vsmGraph.pipelinesInWorkflow("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629", "P4");

        List<VSMGraph.Level> levels = vsmGraph.levels;
        assertThat(pipelines.size(), is(4));
        assertThat(pipelines, contains(levels.get(1).nodes.get(0).id, levels.get(1).nodes.get(1).id,
                levels.get(2).nodes.get(0).id, levels.get(3).nodes.get(0).id));
    }

    @Test
    public void shouldListAllPipelinesBetweenASourcePipelineAndDestinationPipelineIncludingBothTheSourceAndDestination() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(complexGraphWithFanInAndFanOut);

        List<String> pipelines = vsmGraph.pipelinesInWorkflow("P1", "P4");

        List<VSMGraph.Level> levels = vsmGraph.levels;
        assertThat(pipelines.size(), is(3));
        assertThat(pipelines, contains(levels.get(1).nodes.get(0).id, levels.get(2).nodes.get(0).id, levels.get(3).nodes.get(0).id));
    }

    @Test
    public void forADestinationPipelineWithMultipleUpstreamMaterials_shouldShowPipelinesWhichAreBetweenATheSourceMaterialAndDestination() {
        /*
        M1---> P1 --->
                      |
                      P3       Source - M2, Destination - P3
                      |
        M2--->P2 --->
         */

        VSMGraph vsmGraph = VSMGraph.fromJSON(graphForDestinationWithTwoUpstreamMaterials);

        List<String> pipelines = vsmGraph.pipelinesInWorkflow("9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8", "P3");

        assertThat(pipelines.size(), is(2));
        assertThat(pipelines, contains("P2", "P3"));
    }

    String simpleGraph = "{\n" +
            "    \"current_pipeline\": \"P4\",\n" +
            "    \"levels\": [\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"id\": \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "            \"name\": \"material_name\",\n" +
            "            \"parents\": [],\n" +
            "            \"dependents\": [\n" +
            "              \"P1\",\n" +
            "              \"P2\",\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"type\": \"GIT\",\n" +
            "            \"material_revisions\": [\n" +
            "              {\n" +
            "                \"modifications\": [\n" +
            "                  {\n" +
            "                    \"revision\": \"2a13c2e8cf1661d905099e7297dba3c5b58bce7c\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P3\"\n" +
            "            ],\n" +
            "            \"id\": \"P1\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 30,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P1\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }";

    String complexGraphWithFanInAndFanOut = "{\n" +
            "    \"current_pipeline\": \"P4\",\n" +
            "    \"levels\": [\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"id\": \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "            \"name\": \"material_name\",\n" +
            "            \"parents\": [],\n" +
            "            \"dependents\": [\n" +
            "              \"P1\",\n" +
            "              \"P2\",\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"type\": \"GIT\",\n" +
            "            \"material_revisions\": [\n" +
            "              {\n" +
            "                \"modifications\": [\n" +
            "                  {\n" +
            "                    \"revision\": \"2a13c2e8cf1661d905099e7297dba3c5b58bce7c\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P3\"\n" +
            "            ],\n" +
            "            \"id\": \"P1\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 30,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P1\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P3\"\n" +
            "            ],\n" +
            "            \"id\": \"P2\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 52,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P2\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"id\": \"P3\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 13,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P3\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"P1\",\n" +
            "              \"P2\"\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"id\": \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\",\n" +
            "            \"material_revisions\": [\n" +
            "              {\n" +
            "                \"modifications\": [\n" +
            "                  {\n" +
            "                    \"revision\": \"ad67c8a52dd0ed18e722ef526b7818ad0959df19\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"material_name\",\n" +
            "            \"type\": \"GIT\",\n" +
            "            \"parents\": []\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [],\n" +
            "            \"id\": \"P4\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 3,\n" +
            "                \"label\": \"3\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 17,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P4\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"P3\",\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "              \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }";

    String graphForDestinationWithTwoUpstreamMaterials = "{\n" +
            "    \"current_pipeline\": \"P3\",\n" +
            "    \"levels\": [\n" +
            "        {\n" +
            "            \"nodes\": [\n" +
            "                {\n" +
            "                    \"dependents\": [\n" +
            "                        \"P1\"\n" +
            "                    ],\n" +
            "                    \"id\": \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "                    \"material_revisions\": [\n" +
            "                        {\n" +
            "                            \"modifications\": [\n" +
            "                                {\n" +
            "                                    \"revision\": \"6fdb612f5b78eea35e259f8544ddc05330cb5586\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Material1\",\n" +
            "                    \"parents\": [],\n" +
            "                    \"type\": \"GIT\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"dependents\": [\n" +
            "                        \"P2\"\n" +
            "                    ],\n" +
            "                    \"id\": \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\",\n" +
            "                    \"material_revisions\": [\n" +
            "                        {\n" +
            "                            \"modifications\": [\n" +
            "                                {\n" +
            "                                    \"revision\": \"07144631c921de18a838bbfd03263dea8a1af917\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"Material2\",\n" +
            "                    \"parents\": [],\n" +
            "                    \"type\": \"GIT\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"nodes\": [\n" +
            "                {\n" +
            "                    \"dependents\": [\n" +
            "                        \"P3\"\n" +
            "                    ],\n" +
            "                    \"id\": \"P1\",\n" +
            "                    \"instances\": [\n" +
            "                        {\n" +
            "                            \"counter\": 2,\n" +
            "                            \"label\": \"2\",\n" +
            "                            \"stages\": [\n" +
            "                                {\n" +
            "                                    \"counter\": 1,\n" +
            "                                    \"duration\": 15,\n" +
            "                                    \"name\": \"defaultStage\",\n" +
            "                                    \"status\": \"Passed\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"P1\",\n" +
            "                    \"parents\": [\n" +
            "                        \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "                    ],\n" +
            "                    \"type\": \"PIPELINE\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"dependents\": [\n" +
            "                        \"P3\"\n" +
            "                    ],\n" +
            "                    \"id\": \"P2\",\n" +
            "                    \"instances\": [\n" +
            "                        {\n" +
            "                            \"counter\": 2,\n" +
            "                            \"label\": \"2\",\n" +
            "                            \"stages\": [\n" +
            "                                {\n" +
            "                                    \"counter\": 1,\n" +
            "                                    \"duration\": 26,\n" +
            "                                    \"name\": \"defaultStage\",\n" +
            "                                    \"status\": \"Passed\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"P2\",\n" +
            "                    \"parents\": [\n" +
            "                        \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\"\n" +
            "                    ],\n" +
            "                    \"type\": \"PIPELINE\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"nodes\": [\n" +
            "                {\n" +
            "                    \"dependents\": [],\n" +
            "                    \"id\": \"P3\",\n" +
            "                    \"instances\": [\n" +
            "                        {\n" +
            "                            \"counter\": 3,\n" +
            "                            \"label\": \"3\",\n" +
            "                            \"stages\": [\n" +
            "                                {\n" +
            "                                    \"counter\": 1,\n" +
            "                                    \"duration\": 16,\n" +
            "                                    \"name\": \"defaultStage\",\n" +
            "                                    \"status\": \"Passed\"\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"name\": \"P3\",\n" +
            "                    \"parents\": [\n" +
            "                        \"P1\",\n" +
            "                        \"P2\"\n" +
            "                    ],\n" +
            "                    \"type\": \"PIPELINE\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}\n";
}
