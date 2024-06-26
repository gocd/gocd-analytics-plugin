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

import com.thoughtworks.gocd.analytics.models.Job;
import com.thoughtworks.gocd.analytics.models.JobTimeSummary;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface JobMapper {

    @Insert("INSERT INTO jobs(pipeline_name," +
        "pipeline_counter," +
        "stage_name," +
        "stage_counter," +
        "job_name," +
        "result," +
        "scheduled_at," +
        "completed_at," +
        "assigned_at," +
        "time_waiting_secs," +
        "time_building_secs," +
        "duration_secs, " +
        "agent_uuid) values " +
        "(#{pipelineName}," +
        "#{pipelineCounter}," +
        "#{stageName}," +
        "#{stageCounter}," +
        "#{jobName}," +
        "#{result}," +
        "#{scheduledAt}," +
        "#{completedAt}," +
        "#{assignedAt}," +
        "#{timeWaitingSecs}," +
        "#{timeBuildingSecs}," +
        "#{durationSecs}," +
        "#{agentUuid})")
    void insert(Job job);

    @Select("SELECT * FROM jobs where pipeline_name = #{pipelineName}")

    @Results(id = "Job", value = {
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "pipelineCounter", column = "pipeline_counter"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "stageCounter", column = "stage_counter"),
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "result", column = "result"),
        @Result(property = "scheduledAt", column = "scheduled_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "assignedAt", column = "assigned_at"),
        @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
        @Result(property = "timeBuildingSecs", column = "time_building_secs"),
        @Result(property = "durationSecs", column = "duration_secs"),
        @Result(property = "agentUuid", column = "agent_uuid")
    })
    List<Job> allJobs(String pipelineName);

    @Select(
        "  SELECT pipeline_name, stage_name, job_name, AVG(time_waiting_secs) as avg_time_waiting_secs, AVG(time_building_secs) as avg_time_building_secs\n"
            +
            "    FROM jobs\n" +
            "   WHERE agent_uuid = #{agentUUID}\n" +
            "     AND DATE(scheduled_at) >= DATE(#{startDate})\n" +
            "     AND DATE(scheduled_at) <= DATE(#{endDate})\n" +
            "GROUP BY pipeline_name, stage_name, job_name\n" +
            "ORDER BY avg_time_waiting_secs DESC\n" +
            "   LIMIT #{limit};")
    @Results({
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "timeWaitingSecs", column = "avg_time_waiting_secs"),
        @Result(property = "timeBuildingSecs", column = "avg_time_building_secs")
    })
    List<Job> longestWaitingJobsForAgent(@Param("agentUUID") String agentUUID,
        @Param("startDate") ZonedDateTime scheduledBefore,
        @Param("endDate") ZonedDateTime scheduledAfter,
        @Param("limit") int limit);

    @Select(
        "  SELECT pipeline_name, stage_name, job_name, AVG(time_waiting_secs) as avg_time_waiting_secs, AVG(time_building_secs) as avg_build_time_secs FROM jobs\n"
            +
            "   WHERE pipeline_name = #{pipelineName}\n" +
            "     AND DATE(scheduled_at) <= DATE(#{endDate})\n" +
            "     AND DATE(scheduled_at) >= DATE(#{startDate})\n" +
            "     AND result != 'Cancelled'\n" +
            "GROUP BY pipeline_name, stage_name, job_name\n" +
            "ORDER BY avg_time_waiting_secs DESC\n" +
            "   LIMIT #{limit}")

    @Results({
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "timeWaitingSecs", column = "avg_time_waiting_secs"),
        @Result(property = "timeBuildingSecs", column = "avg_build_time_secs")
    })
    List<Job> longestWaitingFor(@Param("pipelineName") String pipelineName,
        @Param("startDate") ZonedDateTime startDate,
        @Param("endDate") ZonedDateTime endDate,
        @Param("limit") int limit);

    @Select(
        "  SELECT pipeline_name, stage_name, job_name, pipeline_counter, stage_counter, time_waiting_secs, time_building_secs, result, scheduled_at\n"
            +
            "    FROM jobs\n" +
            "   WHERE pipeline_name = #{pipelineName}\n" +
            "     AND stage_name = #{stageName}\n" +
            "     AND job_name = #{jobName}\n" +
            "     AND result != 'Cancelled'\n" +
            "ORDER BY scheduled_at ASC")
    @Results({
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "pipelineCounter", column = "pipeline_counter"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "stageCounter", column = "stage_counter"),
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
        @Result(property = "timeBuildingSecs", column = "time_building_secs"),
        @Result(property = "result", column = "result"),
        @Result(property = "scheduledAt", column = "scheduled_at"),
    })
    List<Job> jobHistory(@Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName,
        @Param("jobName") String jobName);

    @Select(
        "  SELECT pipeline_name, stage_name, job_name, pipeline_counter, stage_counter, time_waiting_secs, time_building_secs, result, scheduled_at\n"
            +
            "    FROM jobs\n" +
            "   WHERE agent_uuid = #{agentUUID}\n" +
            "     AND pipeline_name = #{pipelineName}\n" +
            "     AND stage_name = #{stageName}\n" +
            "     AND job_name = #{jobName}\n" +
            "ORDER BY scheduled_at ASC")
    @Results({
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "pipelineCounter", column = "pipeline_counter"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "stageCounter", column = "stage_counter"),
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
        @Result(property = "timeBuildingSecs", column = "time_building_secs"),
        @Result(property = "result", column = "result"),
        @Result(property = "scheduledAt", column = "scheduled_at"),
    })
    List<Job> jobDurationForAgent(@Param("agentUUID") String agentUUID,
        @Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName,
        @Param("jobName") String jobName);

    @Delete("DELETE FROM jobs where scheduled_at AT TIME ZONE 'UTC' < #{scheduled_date} AT TIME ZONE 'UTC';")
    void deleteJobRunsPriorTo(@Param("scheduled_date") ZonedDateTime scheduledDate);

    @ResultMap("Job")
    @Select("WITH MergedData AS (\n"
        + "  SELECT\n"
        + "    DATE(scheduled_at) AS date_only,\n"
        + "    SUM(time_waiting_secs) AS total_time\n"
        + "  FROM jobs\n"
        + "  GROUP BY DATE(scheduled_at)\n"
        + ")\n"
        + "SELECT\n"
        + "  date_only AS scheduled_at,\n"
        + "  total_time AS time_waiting_secs\n"
        + "FROM MergedData;")
    List<Job> allWaitingFor();

    @ResultMap("Job")
    @Select("select distinct * from jobs where stage_name = #{stageName} and pipeline_counter "
        + "between #{pipelineCounterStart} and #{pipelineCounterEnd} order by pipeline_counter "
        + "desc")
    List<Job> allJobsOfStage(@Param("stageName") String stageName,
        @Param("pipelineCounterStart") int pipelineCounterStart,
        @Param("pipelineCounterEnd") int pipelineCounterEnd);


    @Results(id = "job_summary", value = {
        @Result(property = "jobName", column = "job_name"),
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "times", column = "times"),
        @Result(property = "sumTotalTimeSecs", column = "sum_total_time_secs"),
        @Result(property = "sumTimeWaitingSecs", column = "sum_time_waiting_secs"),
        @Result(property = "sumTimeBuildingSecs", column = "sum_time_building_secs")
    })
    @Select("SELECT pipeline_name, stage_name, job_name,\n"
        + "       SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) AS times,\n"
        + "       SUM(CASE WHEN result = #{result} THEN duration_secs ELSE 0 END) AS sum_total_time_secs,\n"
        + "       SUM(CASE WHEN result = #{result} THEN time_waiting_secs ELSE 0 END) AS sum_time_waiting_secs,\n"
        + "       SUM(CASE WHEN result = #{result} THEN time_building_secs ELSE 0 END) AS sum_time_building_secs\n"
        + "FROM jobs\n"
        + "WHERE assigned_at BETWEEN #{startDate} AND #{endDate} \n"
        + "GROUP BY pipeline_name, stage_name, job_name\n"
        + "HAVING SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) > 0\n"
        + "order by times desc;")
    List<JobTimeSummary> jobSummary(@Param("startDate") String startDate,
        @Param("endDate") String endDate, @Param("result") String result);

    @ResultMap("Job")
    @Select("SELECT * FROM jobs j WHERE j.job_name = #{jobName} AND j.result = #{result} ORDER BY"
        + " j.scheduled_at")
    List<Job> jobSummaryDetails(@Param("jobName") String jobName, @Param("result") String result);
}
