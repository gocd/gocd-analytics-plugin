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

package com.thoughtworks.gocd.analytics.dao;

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class JobDAOIntegrationTest implements DAOIntegrationTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private JobDAO dao;
    private int id;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        dao = new JobDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
        id = 0;
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
        id = 0;
    }

    @Test
    public void shouldInsertJobsSuccessfully() {
        List<Job> jobs = new ArrayList<>();

        jobs.add(jobFrom("p1", 1, "s1", 1, "j1", PASSED, 5, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s1", 1, "j2", PASSED, 5, 2, 7, TEST_TS));

        dao.insertJobs(sqlSession, jobs);

        List<Job> result = dao.all(sqlSession, "p1");
        assertArrayEquals(new String[]{"j1", "j2"}, pluckSorted(result, Job::getJobName));
    }

    @Test
    public void shouldReturnLongestWaitingJobs() {
        final int cancelledValue = 99999999;

        List<Job> jobs = new ArrayList<>();
        jobs.add(jobFrom("p1", 1, "s6", 1, "j1", PASSED, 2, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s5", 1, "j1", PASSED, 2, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s7", 1, "j2", PASSED, 7, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s7", 1, "j3", PASSED, 5, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s9", 1, "j4", CANCELLED, cancelledValue, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 2, "s1", 1, "j1", FAILED, 20, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 2, "s1", 1, "j2", PASSED, 7, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 1, "s8", 1, "j1", FAILED, 50, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 2, "s2", 1, "j1", FAILED, 50, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 3, "s2", 1, "j1", FAILED, 25, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 4, "s4", 1, "j1", FAILED, 2, 2, 7, TEST_TS));
        jobs.add(jobFrom("p1", 5, "s3", 1, "j1", FAILED, 2, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 2, "s1", 1, "j2", PASSED, 7, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 1, "s8", 1, "j1", FAILED, 50, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 2, "s2", 1, "j1", FAILED, 50, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 3, "s2", 1, "j1", FAILED, 25, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 4, "s4", 1, "j1", FAILED, 2, 2, 7, TEST_TS));
        jobs.add(jobFrom("p2", 5, "s3", 1, "j1", FAILED, 2, 2, 7, TEST_TS));

        dao.insertJobs(sqlSession, jobs);

        List<Job> result = dao.longestWaitingFor(sqlSession, "p1", TEST_TS.minusDays(1), TEST_TS.plusDays(1), 10);
        assertEquals(10, result.size());
        assertEquals(50, result.get(0).getTimeWaitingSecs());
        for (int i = 1; i < result.size(); i++) {
            assertFalse(result.get(i).getTimeWaitingSecs() == cancelledValue);
            assertTrue(result.get(i).getTimeWaitingSecs() <= result.get(i - 1).getTimeWaitingSecs());
        }
    }

    @Test
    public void shouldReturnLongestWaitingJobsForAnAgent() throws ParseException {
        String AGENT_UUID = "agent-1";

        List<Job> jobs = new ArrayList<>();

        jobs.add(jobFrom("p1", 1, "s1", 1, "j1", PASSED, 1, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p1", 2, "s1", 1, "j1", PASSED, 2, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p1", 3, "s1", 1, "j1", PASSED, 3, 2, 7, AGENT_UUID));

        jobs.add(jobFrom("p2", 1, "s1", 1, "j1", PASSED, 10, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p2", 2, "s1", 1, "j1", PASSED, 6, 2, 7, AGENT_UUID));

        jobs.add(jobFrom("p3", 1, "s1", 1, "j1", PASSED, 7, 2, 7, AGENT_UUID));

        jobs.add(jobFrom("p4", 1, "s1", 1, "j1", PASSED, 3, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p4", 2, "s1", 1, "j1", PASSED, 5, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p4", 3, "s1", 1, "j1", PASSED, 7, 2, 7, AGENT_UUID));
        jobs.add(jobFrom("p4", 4, "s1", 1, "j1", PASSED, 9, 2, 7, AGENT_UUID));

        ZonedDateTime startDate = TEST_TS.minusDays(1).withZoneSameInstant(UTC);
        dao.insertJobs(sqlSession, jobs);
        ZonedDateTime endDate = TEST_TS.plusDays(1).withZoneSameInstant(UTC);

        List<Job> result = dao.longestWaitingJobsForAgent(sqlSession, AGENT_UUID, startDate, endDate, 4);

        ArrayList<Job> expected = new ArrayList<>();
        expected.add(jobFrom("p2", "s1", "j1", 8, 2));
        expected.add(jobFrom("p3", "s1", "j1", 7, 2));
        expected.add(jobFrom("p4", "s1", "j1", 6, 2));
        expected.add(jobFrom("p1", "s1", "j1", 2, 2));

        assertEquals(4, result.size());
        assertEquals(expected, result);
    }

    @Test
    public void shouldReturnAllInstancesOfJobForASpecificPipelineAndAgent() {
        String agentUUID = "agent-uuid";
        String pipelineName = "p1";
        String stageName = "s1";
        String jobName = "j1";

        final int ignored = 0;
        Job instance1 = jobFrom(pipelineName, 1, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(1));
        Job instance2 = jobFrom(pipelineName, 2, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(2));
        Job instance3 = jobFrom(pipelineName, 3, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(3));
        Job instance4 = jobFrom(pipelineName, 4, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(4));
        Job instance5 = jobFrom(pipelineName, 5, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(5));
        Job instance6 = jobFrom(pipelineName, 6, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(6));
        Job instance7 = jobFrom(pipelineName, 7, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(7));
        Job instance8 = jobFrom(pipelineName, 8, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(8));
        Job instance9 = jobFrom(pipelineName, 9, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(9));
        Job instance10 = jobFrom(pipelineName, 10, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS.minusMinutes(10));
        Job anotherPipelineInstance1 = jobFrom("p2", 1, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS);
        Job anotherPipelineInstance2 = jobFrom("p2", 2, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS);
        Job anotherPipelineInstance3 = jobFrom("p2", 3, stageName, 1, jobName, PASSED, 2, 2, ignored, agentUUID, TEST_TS);

        List<Job> jobs = new ArrayList<>();

        jobs.add(instance1);
        jobs.add(instance2);
        jobs.add(instance3);
        jobs.add(instance4);
        jobs.add(instance5);
        jobs.add(instance6);
        jobs.add(instance7);

        jobs.add(anotherPipelineInstance1);
        jobs.add(anotherPipelineInstance2);
        jobs.add(anotherPipelineInstance3);

        jobs.add(instance8);
        jobs.add(instance9);
        jobs.add(instance10);

        dao.insertJobs(sqlSession, jobs);

        ArrayList<Job> expected = new ArrayList<>();
        expected.add(instance10);
        expected.add(instance9);
        expected.add(instance8);
        expected.add(instance7);
        expected.add(instance6);
        expected.add(instance5);
        expected.add(instance4);
        expected.add(instance3);
        expected.add(instance2);
        expected.add(instance1);

        List<Job> result = dao.jobDurationForAgent(sqlSession, agentUUID, pipelineName, stageName, jobName);

        for (int i = 0, len = expected.size(); i < len; ++i) {
            Job exp = expected.get(i);
            Job act = result.get(i);

            assertEquals(exp.getPipelineName(), act.getPipelineName());
            assertEquals(exp.getPipelineCounter(), act.getPipelineCounter());
            assertEquals(exp.getStageName(), act.getStageName());
            assertEquals(exp.getStageCounter(), act.getStageCounter());
            assertEquals(exp.getJobName(), act.getJobName());
            assertEquals(exp.getScheduledAt(), act.getScheduledAt());
        }
    }

    @Test
    public void shouldReturnJobHistory() throws Exception {
        List<Job> cancelledJobs = Arrays.asList(jobsFrom("pip", "stg", "job1", CANCELLED, asZonedDateTime("2018-01-10")));
        dao.insertJobs(sqlSession, cancelledJobs);

        List<Job> jobsWithDifferentIdentifier = Arrays.asList(jobsFrom("pip2", "stg", "job1", PASSED, asZonedDateTime("2018-01-10")),
                jobsFrom("pip", "stg2", "job1", FAILED, asZonedDateTime("2018-01-10")),
                jobsFrom("pip", "stg", "job2", PASSED, asZonedDateTime("2018-01-10")));

        dao.insertJobs(sqlSession, jobsWithDifferentIdentifier);

        Job job1 = jobsFrom("pip", "stg", "job1", PASSED, asZonedDateTime("2018-01-11"));
        Job job2 = jobsFrom("pip", "stg", "job1", FAILED, asZonedDateTime("2018-01-10"));

        dao.insertJobs(sqlSession, Arrays.asList(job1, job2));

        final List<Job> fetchedJobs = dao.jobHistory(sqlSession, "pip", "stg", "job1");

        assertEquals(2, fetchedJobs.size());
        assertEquals(job2, fetchedJobs.get(0));
        assertEquals(job1, fetchedJobs.get(1));
    }

    @Test
    public void shouldDeleteAllJobsScheduledOnOrBeforeAGivenScheduledDate() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-02T00:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));

        dao.insertJobs(sqlSession, Arrays.asList(jobFrom("pipeline_name", 1, "stage_name", 1,
                "job_name", "passed", 1, 1, 1, dateTime),
                jobFrom("pipeline_name", 1, "stage_name", 1,
                        "job_name", "passed", 1, 1, 1, dateTime.minusHours(1)),
                jobFrom("pipeline_name", 1, "stage_name", 1,
                        "job_name", "passed", 1, 1, 1, dateTime.plusHours(1))));

        assertEquals(3, dao.all(sqlSession, "pipeline_name").size());

        dao.deleteJobRunsPriorTo(sqlSession, dateTime);

        assertThat(dao.all(sqlSession, "pipeline_name"))
                .hasSize(2)
                .extracting(Job::getScheduledAt)
                .extracting(ZonedDateTime::toEpochSecond)
                .containsExactlyInAnyOrder(dateTime.plusHours(1).toEpochSecond(), dateTime.toEpochSecond());
    }

    private Object[] pluckSorted(List<Job> jobs, Function<Job, Object> getterRef) {
        final Object[] ids = jobs.stream().map(getterRef).toArray();
        Arrays.sort(ids);
        return ids;
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                        String jobName, String result, int timeWaiting, int timeBuilding, int duration, String agentUUID, ZonedDateTime scheduledAt) {
        Job job = jobFrom(pipelineName, pipelineCounter, stageName, stageCounter, jobName, result, timeWaiting, timeBuilding, duration, TEST_TS);
        job.setAgentUuid(agentUUID);
        job.setScheduledAt(scheduledAt);
        return job;
    }


    private Job jobsFrom(String pipelineName, String stageName,
                         String jobName, String result, ZonedDateTime scheduledAt) {
        Job job = new Job();
        job.setPipelineName(pipelineName);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setResult(result);
        job.setScheduledAt(scheduledAt);
        return job;
    }


    private Job jobsFrom(String pipelineName, String stageName,
                         String jobName, String result, ZonedDateTime scheduledAt, int timeWaitingSecs, int timeBuildingSecs) {
        Job job = new Job();
        job.setPipelineName(pipelineName);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setResult(result);
        job.setScheduledAt(scheduledAt);
        job.setTimeWaitingSecs(timeWaitingSecs);
        job.setTimeBuildingSecs(timeBuildingSecs);
        return job;
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                        String jobName, String result, int timeWaiting,
                        int timeBuilding, int duration, ZonedDateTime scheduledAt) {
        Job job = new Job();
        job.setId(id++);
        job.setPipelineName(pipelineName);
        job.setPipelineCounter(pipelineCounter);
        job.setStageName(stageName);
        job.setStageCounter(stageCounter);
        job.setJobName(jobName);
        job.setResult(result);
        job.setScheduledAt(scheduledAt);
        job.setCompletedAt(TEST_TS);
        job.setAssignedAt(TEST_TS);
        job.setTimeWaitingSecs(timeWaiting);
        job.setTimeBuildingSecs(timeBuilding);
        job.setDurationSecs(duration);
        job.setAgentUuid(null);

        return job;
    }

    private Job jobFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                        String jobName, String result, int timeWaiting, int timeBuilding, int duration, String agentUUID) {
        Job job = jobFrom(pipelineName, pipelineCounter, stageName, stageCounter, jobName, result, timeWaiting, timeBuilding, duration, TEST_TS);
        job.setAgentUuid(agentUUID);
        return job;
    }

    private Job jobFrom(String pipelineName, String stageName, String jobName, int timedWaitingInSecs, int timedBuildingInSecs) {
        Job job = new Job();

        job.setPipelineName(pipelineName);
        job.setStageName(stageName);
        job.setJobName(jobName);
        job.setTimeWaitingSecs(timedWaitingInSecs);
        job.setTimeBuildingSecs(timedBuildingInSecs);

        return job;
    }

    private ZonedDateTime asZonedDateTime(String isoDateOnly) {
        return DateUtils.parseDate(isoDateOnly);
    }
}
