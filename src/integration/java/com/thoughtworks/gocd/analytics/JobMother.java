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

import com.thoughtworks.gocd.analytics.models.Job;

import java.time.ZonedDateTime;

public class JobMother {
    public static Job jobWith(String pipelineName, int counter, String stageName, String jobName, String result, ZonedDateTime scheduledAt) {
        Job job = new Job();
        job.setPipelineName(pipelineName);
        job.setPipelineCounter(counter);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setResult(result);
        job.setScheduledAt(scheduledAt);

        return job;
    }

    public static Job jobWith(String pipelineName, int counter, String stageName, String jobName, String result, ZonedDateTime scheduledAt, String agentUuid) {
        Job job = jobWith(pipelineName, counter, stageName, jobName, result, scheduledAt);
        job.setAgentUuid(agentUuid);
        return job;
    }

}
