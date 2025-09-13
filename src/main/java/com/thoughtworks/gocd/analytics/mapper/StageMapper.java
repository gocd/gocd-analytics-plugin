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

import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.models.StageTimeSummary;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface StageMapper {

    @Insert("INSERT INTO stages(pipeline_name," +
        "pipeline_counter," +
        "stage_name," +
        "stage_counter," +
        "result," +
        "state," +
        "approval_type," +
        "approved_by," +
        "scheduled_at," +
        "completed_at," +
        "duration_secs," +
        "time_waiting_secs) values (#{pipelineName}," +
        "#{pipelineCounter}," +
        "#{stageName}," +
        "#{stageCounter}," +
        "#{result}," +
        "#{state}," +
        "#{approvalType}," +
        "#{approvedBy}," +
        "#{scheduledAt}," +
        "#{completedAt}," +
        "#{totalTimeSecs}," +
        "#{timeWaitingSecs})")
    @SelectKey(statement = "SELECT lastval()", keyProperty = "id", keyColumn = "id", before = false, resultType = long.class)
    void insert(Stage stage);

    @Results(id = "Stage", value = {
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "pipelineCounter", column = "pipeline_counter"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "stageCounter", column = "stage_counter"),
        @Result(property = "result", column = "result"),
        @Result(property = "state", column = "state"),
        @Result(property = "approvalType", column = "approval_type"),
        @Result(property = "approvedBy", column = "approved_by"),
        @Result(property = "scheduledAt", column = "scheduled_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalTimeSecs", column = "duration_secs"),
        @Result(property = "timeWaitingSecs", column = "time_waiting_secs")
    })
    @Select("SELECT * FROM stages WHERE pipeline_name = #{pipelineName}")
    List<Stage> allStages(@Param("pipelineName") String pipelineName);

    @ResultMap("Stage")
    @Select("SELECT *\n" +
        "  FROM stages\n" +
        " WHERE pipeline_name = #{pipelineName}\n" +
        "   AND (DATE(#{startDate}) IS NULL OR DATE(scheduled_at) >= DATE(#{startDate}))\n" +
        "   AND (DATE(#{endDate}) IS NULL OR DATE(scheduled_at) <= DATE(#{endDate}))\n" +
        "   AND stage_name = #{stageName}")
    List<Stage> stageHistory(@Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName,
        @Param("startDate") ZonedDateTime startDate,
        @Param("endDate") ZonedDateTime endDate);

    @ResultMap("Stage")
    @Select("  SELECT *\n" +
        "    FROM stages\n" +
        "   WHERE pipeline_name = #{pipelineName}\n" +
        "     AND pipeline_counter = #{pipelineCounter}\n" +
        "     AND stage_name = #{stageName}\n" +
        "     AND stage_counter = #{stageCounter}\n" +
        "ORDER BY completed_at\n" +
        "   LIMIT 1")
    Stage One(@Param("pipelineName") String pipelineName,
        @Param("pipelineCounter") int pipelineCounter,
        @Param("stageName") String stageName,
        @Param("stageCounter") int stageCounter);

    @Delete("DELETE FROM stages where scheduled_at AT TIME ZONE 'UTC' < #{scheduled_date} AT TIME ZONE 'UTC';")
    void deleteStageRunsPriorTo(@Param("scheduled_date") ZonedDateTime scheduledDate);

    @ResultMap("Stage")
    @Select("<script>" +
        "  SELECT * FROM stages " +
        "    WHERE (id IN (SELECT stage_id FROM pipeline_workflows WHERE workflow_id=#{workflowId}))"
        +
        "      AND pipeline_name IN " +
        "      <foreach item='item' index='index' collection='pipelines'" +
        "        open='(' separator=',' close=')'>" +
        "        #{item}" +
        "      </foreach>" +
        "</script>")
    List<Stage> allStageInstancesWithWorkflowIdOfPipelines(@Param("workflowId") Long workflowID,
        @Param("pipelines") List<String> pipelines);

    @ResultMap("Stage")
//    @Select("select * from stages where id in (select stage_id from pipeline_workflows pw where "
//        + "pipeline_id in (select id from pipelines p where name = #{pipelineName} limit 10))")
    @Select("<script>"
        + "select * from stages where "
        + "<if test='result != null'>"
        + "result = #{result} and "
        + "</if>"
        + "id in (select stage_id from pipeline_workflows pw where "
        + "pipeline_id in (select id from pipelines p where name = #{pipelineName} "
        + "and created_at between #{startDate} and #{endDate} "
        + "order by id "
        + "<if test='order != null'>"
        + "desc "
        + "</if>"
        + "limit #{limit})) "
        + "</script>"
    )
    List<Stage> stageByPipelineNameAndCounter(@Param("startDate") String startDate,
        @Param("endDate") String endDate, @Param("pipelineName") String pipelineName,
        @Param("result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("SELECT pipeline_name, stage_name, pipeline_counter, MAX(stage_counter) AS "
        + "stage_counter\n"
        + "FROM stages s \n"
        + "where scheduled_at >= date(#{startDate}) and scheduled_at <= date(#{endDate}) \n"
        + "GROUP BY pipeline_name, stage_name, pipeline_counter \n"
        + "HAVING MAX(stage_counter) > 1\n"
        + "order by stage_counter ${order} limit #{limit} ;"
    )
    List<Stage> stageRerunsForAllPipelines(@Param("result") String result,
        @Param("startDate") String startDate, @Param("endDate") String endDate,
        @Param("order") String order, @Param("limit") int limit);

//    @Select("<script>"
//        + "SELECT pipeline_name, stage_name, pipeline_counter, MAX(stage_counter) AS stage_counter\n"
//        + "FROM stages s \n"
//        + "WHERE pipeline_name = #{pipelineName} \n"
//        + "GROUP BY pipeline_name, stage_name, pipeline_counter\n"
//        + "HAVING MAX(stage_counter) > 1\n"
//        + "order by stage_counter ${order} limit #{limit} ;"
//        + "</script>"
//    )

    @ResultMap("Stage")

//    @Select("<script>"
//        + "SELECT pipeline_name, stage_name, pipeline_counter, MAX(stage_counter) AS stage_counter\n"
//        + "FROM stages s \n"
//        + "WHERE pipeline_name = #{pipelineName} \n"
//        + "GROUP BY pipeline_name, stage_name, pipeline_counter\n"
//        + "HAVING MAX(stage_counter) > 1\n"
//        + "order by stage_counter ${order} limit #{limit} ;"
//        + "</script>"
//    )

    // ought to work in postgres
//    @Select("<script>"
//        + "SELECT pipeline_name, stage_name, pipeline_counter, stage_counter, scheduled_at\n"
//        + "FROM (\n"
//        + "SELECT DISTINCT ON (s.pipeline_name, s.stage_name, s.pipeline_counter)\n"
//        + "s.pipeline_name,\n"
//        + "s.stage_name,\n"
//        + "s.pipeline_counter,\n"
//        + "s.stage_counter,\n"
//        + "s.scheduled_at\n"
//        + "FROM stages s \n"
//        + "WHERE s.scheduled_at >= DATE(#{startDate}) AND s.scheduled_at <= DATE(#{endDate})\n"
//        + "AND s.pipeline_name = #{pipelineName}\n"
//        + "ORDER BY s.pipeline_name, s.stage_name, s.pipeline_counter, s.stage_counter DESC\n"
//        + ") AS subquery_results\n"
//        + "WHERE stage_counter > 1\n"
//        + "ORDER BY scheduled_at DESC\n"
//        + "LIMIT #{limit};"
//        + "</script>"
//    )

    // Postgres specific
    // Casting scheduled_at column to DATE is not performant
    // Recommended way to do the same is bound the date to < next_date

    @Select(
//        "<script>"
        "SELECT pipeline_name, stage_name, pipeline_counter, stage_counter, scheduled_at"
        +" FROM ("
        +"  SELECT DISTINCT ON (s.pipeline_name, s.stage_name, s.pipeline_counter)"
        +"         s.pipeline_name,"
        +"         s.stage_name,"
        +"         s.pipeline_counter,"
        +"         s.stage_counter,"
        +"         s.scheduled_at"
        +"  FROM stages s"
        +"  WHERE s.scheduled_at::DATE >= DATE(#{startDate})"
        +"    AND s.scheduled_at::DATE <= DATE(#{endDate})"
        +"    AND s.pipeline_name = #{pipelineName}"
        +"  ORDER BY s.pipeline_name, s.stage_name, s.pipeline_counter, s.stage_counter DESC"
        +" ) AS subquery_results"
        +" WHERE stage_counter > 1"
        +" ORDER BY scheduled_at DESC"
        +" LIMIT #{limit}"
//        +"</script>"
    )

    // Standard SQL

//    @Select(
//        "<script>"
//        +
//            "SELECT pipeline_name, stage_name, pipeline_counter, stage_counter, scheduled_at\n"
//            + "FROM (\n"
//            + "    SELECT s.pipeline_name,\n"
//            + "           s.stage_name,\n"
//            + "           s.pipeline_counter,\n"
//            + "           s.stage_counter,\n"
//            + "           s.scheduled_at,\n"
//            + "           ROW_NUMBER() OVER (\n"
//            + "               PARTITION BY s.pipeline_name, s.stage_name, s.pipeline_counter\n"
//            + "               ORDER BY s.stage_counter DESC\n"
//            + "           ) AS rn\n"
//            + "    FROM stages s\n"
//            + "    WHERE s.scheduled_at >= CAST(#{startDate} AS DATE)\n"
//            + "      AND s.scheduled_at <= CAST(#{endDate} AS DATE)\n"
//            + "      AND s.pipeline_name = #{pipelineName}\n"
//            + ") t\n"
//            + "WHERE rn = 1\n"
//            + "  AND stage_counter > 1\n"
//            + "ORDER BY scheduled_at DESC\n"
//            + "LIMIT 10;"
//        + "</script>"
//    )

    List<Stage> stageReruns(@Param("startDate") String startDate,
        @Param("endDate") String endDate, @Param("pipelineName") String pipelineName,
        @Param("result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "select * from stages \n"
        + "where pipeline_name = #{pipelineName} \n"
        + "and stage_name = #{stageName} \n"
        + "and pipeline_counter = #{pipelineCounter} \n"
        + "order by id asc \n"
        + "</script>"
    )
    List<Stage> stageRerunsForPipelineStageAndCounter(@Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName, @Param("pipelineCounter") int pipelineCounter,
        @Param("result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "SELECT *\n"
        + "FROM (\n"
        + "  SELECT *, ROW_NUMBER() OVER (PARTITION BY pipeline_counter ORDER BY id ASC) AS row_num\n"
        + "  FROM stages\n"
        + "  WHERE pipeline_name = #{pipelineName}\n"
        + "  AND scheduled_at between #{startDate} AND #{endDate} \n"
        + "  <if test='result != null'>"
        + "  AND result = #{result} \n"
        + "  </if>"
        + ") AS ranked_data\n"
        + "WHERE row_num = 1 \n"
        + "ORDER BY id ${order} LIMIT #{limit} ;"
        + "</script>"
    )
    List<Stage> stageStartupTime(@Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("pipelineName") String pipelineName,
        @Param("result") String result,
        @Param("order") String order,
        @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "select * from stages where pipeline_name = #{pipelineName} \n"
        + "and pipeline_counter = #{pipelineCounter} \n"
        + "order by id;"
        + "</script>"
    )
    List<Stage> stageStartupTimeCompare(@Param("pipelineName") String pipelineName,
        @Param("pipelineCounter") int pipelineCounter);

    @Results(id = "stage_summary", value = {
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "times", column = "times"),
        @Result(property = "sumTotalTimeSecs", column = "sum_total_time_secs"),
        @Result(property = "sumTimeWaitingSecs", column = "sum_time_waiting_secs")
    })
    @Select("SELECT pipeline_name, stage_name,\n"
        + "       SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) AS times,\n"
        + "       SUM(CASE WHEN result = #{result} THEN duration_secs ELSE 0 END) AS sum_total_time_secs,\n"
        + "       SUM(CASE WHEN result = #{result} THEN time_waiting_secs ELSE 0 END) AS sum_time_waiting_secs\n"
        + "FROM stages\n"
        + "WHERE scheduled_at BETWEEN #{startDate} AND #{endDate} \n"
        + "GROUP BY pipeline_name, stage_name\n"
        + "HAVING SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) > 0\n"
        + "order by times desc;")
    List<StageTimeSummary> stageSummary(@Param("startDate") String startDate,
        @Param("endDate") String endDate, @Param("result") String result);

    @ResultMap("Stage")
    @Select("SELECT * FROM stages s WHERE s.stage_name = #{stageName} and s.result = #{result} "
        + "ORDER BY s.scheduled_at")
    List<Stage> stageSummaryDetails(@Param("stageName") String stageName,
        @Param("result") String result);
}
