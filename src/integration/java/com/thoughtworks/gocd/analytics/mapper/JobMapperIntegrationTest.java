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

package com.thoughtworks.gocd.analytics.mapper;

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class JobMapperIntegrationTest {
    private TestDBConnectionManager manager;
    private JobMapper mapper;

    @Before
    public void before() throws SQLException, InterruptedException {
        manager = new TestDBConnectionManager();
        mapper = manager.getSqlSession().getMapper(JobMapper.class);
    }

    @After
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldConsiderPipelineStageAndJobNamesToBeCaseInsensitive() {
        Job testJob = jobFrom("PIPELINE", "STAGE", "JOB");

        mapper.insert(testJob);

        assertEquals(1, mapper.allJobs("PIPELINE").size());
        assertEquals(testJob, mapper.allJobs("PIPELINE").get(0));
        assertEquals(testJob, mapper.allJobs("pipeline").get(0));

        assertEquals(testJob, mapper.jobHistory("PIPelINE", "StAGE", "JoB").get(0));
        assertEquals(testJob, mapper.jobHistory("pipeline", "STAGE", "Job").get(0));
        assertEquals(testJob, mapper.jobHistory("PIPELINE", "stage", "jOB").get(0));
    }

    /**
     * Creates a test job, but doesn't set all properties because certain queries don't return all columns
     * and will thus fail assertEquals()
     * <p>
     * It just sets enough to work, and uses some random values to ensure that assertEquals doesn't just
     * succeed because of common magic values or defaults.
     *
     * @param pipelineName String
     * @param stageName    String
     * @param jobName      String
     * @return Job
     */
    private Job jobFrom(String pipelineName, String stageName, String jobName) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        final ZonedDateTime randomDate = new Date(rand.nextLong(System.currentTimeMillis())).toInstant().atZone(DateUtils.UTC);

        Job job = new Job();
        job.setPipelineName(pipelineName);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setScheduledAt(randomDate);

        // make these random to show we're fetching real data, and not something static
        job.setResult(rand.nextBoolean() ? "Passed" : "Failed");
        job.setTimeWaitingSecs(rand.nextInt(Integer.MAX_VALUE));
        job.setTimeBuildingSecs(rand.nextInt(Integer.MAX_VALUE));

        return job;
    }
}
