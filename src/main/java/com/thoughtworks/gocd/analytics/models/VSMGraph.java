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

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class VSMGraph {
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Node.class, new NodeAdapter())
            .create();

    @SerializedName("current_pipeline")
    public String currentPipeline;

    @SerializedName("current_material")
    public String currentMaterial;

    public List<Level> levels;

    private Map<String, Node> nodeIdToNodeMap;

    private Map<Node, Integer> nodeToNodeIndex;

    public static VSMGraph fromJSON(String json) {
        return GSON.fromJson(json, VSMGraph.class);
    }

    public Node nodeById(String nodeId) {
        return getNodeIdToNodeMap().get(nodeId);
    }

    public List<String> pipelinesInWorkflow(String source, String destination) {
        VSMGraph.Node sourceNode = this.nodeById(source);
        VSMGraph.Node destinationNode = this.nodeById(destination);

        List<String> pipelineNames = nodesInWorkflow(sourceNode.id, destinationNode.id)
                .stream().map(n -> n.id).collect(toList());

        if (!sourceNode.isPipelineNode()) {
            pipelineNames.remove(source);
        }

        return pipelineNames;
    }

    private List<Node> sourceNodeDescendants(Node source, Node destination) {
        List<Node> descendants = new ArrayList<>();

        traverseDownstream(indexOf(destination), source.dependents, descendants);

        return descendants;
    }

    private void traverseDownstream(int destinationIndex, List<String> dependents, List<Node> descendants) {
        Map<String, Node> nodeNameToNodeMap = getNodeIdToNodeMap();
        dependents.stream().forEach(dependent -> {
            VSMGraph.Node dependentNode = nodeNameToNodeMap.get(dependent);
            if (indexOf(dependentNode) < destinationIndex && "PIPELINE".equalsIgnoreCase(dependentNode.type)) {
                if (!descendants.contains(dependentNode)) {
                    descendants.add(dependentNode);
                }
            }

            traverseDownstream(destinationIndex, dependentNode.dependents, descendants);
        });
    }

    private List<Node> nodesInWorkflow(String source, String destination) {
        Node sourceNode = getNodeIdToNodeMap().get(source);
        Node destinationNode = getNodeIdToNodeMap().get(destination);

        List<Node> nodes = Stream.of(sourceNode, destinationNode).collect(toList());

        traverseUpstream(indexOf(sourceNode), destinationNode.parents, nodes, sourceNodeDescendants(sourceNode, destinationNode));

        nodes.sort(Comparator.comparingInt(this::indexOf));

        return nodes;
    }

    private void traverseUpstream(int sourceIndex, List<String> parents, List<Node> nodes, List<Node> sourceDescendants) {
        Map<String, Node> nodeNameToNodeMap = getNodeIdToNodeMap();
        parents.stream().forEach(parent -> {
            VSMGraph.Node parentNode = nodeNameToNodeMap.get(parent);
            if (indexOf(parentNode) > sourceIndex && "PIPELINE".equalsIgnoreCase(parentNode.type) && sourceDescendants.contains(parentNode)) {
                if (!nodes.contains(parentNode)) {
                    nodes.add(parentNode);
                }
            }

            traverseUpstream(sourceIndex, parentNode.parents, nodes, sourceDescendants);
        });
    }

    private int indexOf(Node node) {
        if (nodeToNodeIndex == null) {
            nodeToNodeIndex = new HashMap<>();
            for (int i = 0; i < levels.size(); ++i) {
                int finalI = i;
                levels.get(i).nodes.stream().forEach(n -> nodeToNodeIndex.put(n, finalI));
            }
        }

        return nodeToNodeIndex.get(node);
    }

    private Map<String, Node> getNodeIdToNodeMap() {
        if (nodeIdToNodeMap == null) {
            nodeIdToNodeMap = new HashMap<>();
            levels.forEach(level -> level.nodes.forEach(node -> nodeIdToNodeMap.put(node.id, node)));
        }

        return nodeIdToNodeMap;
    }

    public static class Level {
        @SerializedName("nodes")
        public List<Node> nodes;
    }

    public static class Node {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        public String type;
        public List<String> parents;
        public List<String> dependents;

        public boolean isPipelineNode() {
            return "PIPELINE".equalsIgnoreCase(type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(id, node.id) &&
                    Objects.equals(name, node.name) &&
                    Objects.equals(type, node.type) &&
                    Objects.equals(parents, node.parents) &&
                    Objects.equals(dependents, node.dependents);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, type, parents, dependents);
        }
    }

    public static class MaterialNode extends Node {
        @SerializedName("material_revisions")
        public List<MaterialRevision> materialRevisions;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            MaterialNode that = (MaterialNode) o;
            return Objects.equals(materialRevisions, that.materialRevisions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), materialRevisions);
        }
    }

    public static class PipelineNode extends Node {
        @SerializedName("instances")
        public List<Instance> instances;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            PipelineNode that = (PipelineNode) o;
            return Objects.equals(instances, that.instances);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), instances);
        }
    }

    public static class MaterialRevision {
        public List<Modification> modifications;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MaterialRevision that = (MaterialRevision) o;
            return Objects.equals(modifications, that.modifications);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modifications);
        }
    }

    public static class Modification {
        @SerializedName("revision")
        public String revision;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Modification that = (Modification) o;
            return Objects.equals(revision, that.revision);
        }

        @Override
        public int hashCode() {
            return Objects.hash(revision);
        }
    }

    public static class Instance {
        @SerializedName("counter")
        public int counter;
        @SerializedName("label")
        public String label;
        @SerializedName("stages")
        public List<Stage> stages;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return counter == instance.counter &&
                    Objects.equals(label, instance.label) &&
                    Objects.equals(stages, instance.stages);
        }

        @Override
        public int hashCode() {
            return Objects.hash(counter, label, stages);
        }
    }

    public static class Stage {
        @SerializedName("name")
        public String name;
        @SerializedName("status")
        public String status;
        @SerializedName("duration")
        public int duration;
        @SerializedName("counter")
        public int counter;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Stage stage = (Stage) o;
            return duration == stage.duration &&
                    counter == stage.counter &&
                    Objects.equals(name, stage.name) &&
                    Objects.equals(status, stage.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, status, duration, counter);
        }
    }

    static class NodeAdapter implements JsonSerializer<Node>, JsonDeserializer<Node> {
        @Override
        public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject asJsonObject = json.getAsJsonObject();
            String type = asJsonObject.get("type").getAsString();
            if ("PIPELINE".equalsIgnoreCase(type)) {
                return context.deserialize(json, PipelineNode.class);
            } else {
                return context.deserialize(json, MaterialNode.class);
            }
        }

        @Override
        public JsonElement serialize(Node src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }
}
