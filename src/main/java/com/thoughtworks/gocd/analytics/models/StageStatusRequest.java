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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;


public class StageStatusRequest {
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new DefaultZonedDateTimeTypeAdapter())
            .create();

    public Pipeline pipeline;

    public static StageStatusRequest fromJSON(String json) {
        return GSON.fromJson(json, StageStatusRequest.class);
    }

    public static class Pipeline {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("group")
        public String group;

        @SerializedName("build-cause")
        public List<BuildCause> buildCause;

        @SerializedName("stage")
        public Stage stage;
    }

    public static class BuildCause {
        @SerializedName("material")
        public Map<String, Object> material;

        @SerializedName("changed")
        public Boolean changed;

        @SerializedName("modifications")
        public List<Modification> modifications;

        public String fingerprint() {
            return (String) material.get("fingerprint");
        }

        public String type() {
            return (String) material.get("type");
        }
    }

    public static class Stage {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("approval-type")
        public String approvalType;

        @SerializedName("approved-by")
        public String approvedBy;

        @SerializedName("previous-stage-name")
        public String previousStageName;

        @SerializedName("previous-stage-counter")
        public int previousStageCounter;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("create-time")
        public ZonedDateTime createTime;

        @SerializedName("last-transition-time")
        public ZonedDateTime lastTransitionTime;
        public List<Job> jobs;

        public boolean isReRun() {
            return Integer.valueOf(counter) > 1;
        }

        public boolean isCompleted() {
            return "Cancelled".equalsIgnoreCase(state)
                    || "Passed".equalsIgnoreCase(state)
                    || "Failed".equalsIgnoreCase(state);
        }

        public ZonedDateTime getCreateTime() {
            return createTime;
        }

        public ZonedDateTime getLastTransitionTime() {
            return lastTransitionTime;
        }
    }

    public static class Job {
        @SerializedName("name")
        public String name;

        @SerializedName("schedule-time")
        public ZonedDateTime scheduleTime;

        @SerializedName("complete-time")
        public ZonedDateTime completeTime;

        @SerializedName("assign-time")
        public ZonedDateTime assignTime;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("agent-uuid")
        public String agentUuid;

        public int duration() {
            return Math.toIntExact(Duration.between(scheduleTime.toInstant(), completeTime.toInstant()).getSeconds());
        }

        public int timeWaiting() {
            Instant endTime = assignTime != null ? assignTime.toInstant() : completeTime.toInstant();

            return Math.toIntExact(Duration.between(scheduleTime.toInstant(), endTime).getSeconds());
        }

        public int timeBuilding() {
            return assignTime != null ?
                    Math.toIntExact(Duration.between(assignTime.toInstant(), completeTime.toInstant()).getSeconds()) :
                    0;
        }

        public ZonedDateTime getScheduleTime() {
            return scheduleTime;
        }

        public ZonedDateTime getCompleteTime() {
            return completeTime;
        }

        public ZonedDateTime getAssignTime() {
            return assignTime;
        }
    }

    public static class Modification {
        @SerializedName("revision")
        public String revision;

        @SerializedName("modified-time")
        public ZonedDateTime modifiedTime;

        @SerializedName("data")
        public Map<String, String> data;

        public ZonedDateTime getModifiedTime() {
            return modifiedTime;
        }
    }
}

