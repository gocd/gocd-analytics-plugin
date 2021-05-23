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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VSMGraphTest {
    @Test
    public void shouldSerializeFromJSON() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(simpleGraph);

        assertEquals("P4", vsmGraph.currentPipeline);
        assertEquals(2, vsmGraph.levels.size());

        assertEquals(1, vsmGraph.levels.get(0).nodes.size());
        VSMGraph.MaterialNode materialNode = (VSMGraph.MaterialNode) vsmGraph.levels.get(0).nodes.get(0);

        assertEquals("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629", materialNode.id);
        assertTrue(materialNode.dependents.containsAll(Set.of("P1", "P2", "P4")));
        assertEquals(0, materialNode.parents.size());
        assertEquals("GIT", materialNode.type);
        assertEquals(1, materialNode.materialRevisions.size());
        assertEquals(1, materialNode.materialRevisions.get(0).modifications.size());
        assertEquals("2a13c2e8cf1661d905099e7297dba3c5b58bce7c", materialNode.materialRevisions.get(0).modifications.get(0).revision);

        assertEquals(1, vsmGraph.levels.get(1).nodes.size());
        VSMGraph.PipelineNode pipelineNode = (VSMGraph.PipelineNode) vsmGraph.levels.get(1).nodes.get(0);

        assertEquals("P1", pipelineNode.id);
        assertTrue(pipelineNode.parents.contains("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629"));
        assertTrue(pipelineNode.dependents.contains("P3"));
        assertEquals("PIPELINE", pipelineNode.type);
        assertEquals(1, pipelineNode.instances.size());
        assertEquals(1, pipelineNode.instances.get(0).counter);
        assertEquals("1", pipelineNode.instances.get(0).label);
        assertEquals("1", pipelineNode.instances.get(0).label);
        assertEquals(1, pipelineNode.instances.get(0).stages.size());
        assertEquals("defaultStage", pipelineNode.instances.get(0).stages.get(0).name);
        assertEquals(30, pipelineNode.instances.get(0).stages.get(0).duration);
        assertEquals("Passed", pipelineNode.instances.get(0).stages.get(0).status);
        assertEquals(1, pipelineNode.instances.get(0).stages.get(0).counter);
    }

    @Test
    public void shouldListAllPipelinesBetweenASourceMaterialAndDestinationPipelineExcludingTheSourceMaterial() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(complexGraphWithFanInAndFanOut);

        List<String> pipelines = vsmGraph.pipelinesInWorkflow("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629", "P4");

        List<VSMGraph.Level> levels = vsmGraph.levels;
        assertEquals(4, pipelines.size());
        assertEquals(List.of(levels.get(1).nodes.get(0).id, levels.get(1).nodes.get(1).id,
                levels.get(2).nodes.get(0).id, levels.get(3).nodes.get(0).id), pipelines);
    }

    @Test
    public void shouldListAllPipelinesBetweenASourcePipelineAndDestinationPipelineIncludingBothTheSourceAndDestination() {
        VSMGraph vsmGraph = VSMGraph.fromJSON(complexGraphWithFanInAndFanOut);

        List<String> pipelines = vsmGraph.pipelinesInWorkflow("P1", "P4");

        List<VSMGraph.Level> levels = vsmGraph.levels;
        assertEquals(3, pipelines.size());
        assertEquals(List.of(levels.get(1).nodes.get(0).id, levels.get(2).nodes.get(0).id, levels.get(3).nodes.get(0).id), pipelines);
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

        assertEquals(2, pipelines.size());
        assertEquals(List.of("P2", "P3"), pipelines);
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
