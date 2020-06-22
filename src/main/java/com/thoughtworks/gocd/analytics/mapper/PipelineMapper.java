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


import com.thoughtworks.gocd.analytics.models.Pipeline;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface PipelineMapper {

    @Insert("INSERT INTO pipelines(name," +
            "counter," +
            "result," +
            "total_time_secs," +
            "time_waiting_secs," +
            "created_at," +
            "last_transition_time) VALUES " +
            "(#{name}," +
            "#{counter}," +
            "#{result}," +
            "#{totalTimeSecs}," +
            "#{timeWaitingSecs}," +
            "#{createdAt}," +
            "#{lastTransitionTime});")
    @SelectKey(statement="SELECT lastval()", keyProperty="id", keyColumn = "id", before=false, resultType=long.class)
    void insert(PipelineInstance pipeline);

    @Update("UPDATE pipelines\n" +
            "   SET result = #{result},\n" +
            "       total_time_secs = (total_time_secs + #{totalTimeSecs}),\n" +
            "       time_waiting_secs = (time_waiting_secs + #{timeWaitingSecs}),\n" +
            "       last_transition_time = #{lastTransitionTime}\n" +
            " WHERE id = #{id}")
    void update(PipelineInstance pipeline);

    @Results(id = "PipelineInstance", value = {
            @Result(property = "name", column = "name"),
            @Result(property = "counter", column = "counter"),
            @Result(property = "totalTimeSecs", column = "total_time_secs"),
            @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "lastTransitionTime", column = "last_transition_time"),
            @Result(property = "result", column = "result")
    })
    @Select("SELECT * FROM pipelines WHERE name = #{name} AND counter = #{counter} LIMIT 1")
    PipelineInstance findInstance(@Param("name") String name, @Param("counter") int counter);

    @ResultMap("PipelineInstance")
    @Select("  SELECT *\n" +
            "    FROM pipelines\n" +
            "   WHERE name = #{name}\n" +
            "     AND (DATE(#{startDate}) IS NULL OR DATE(created_at) >= DATE(#{startDate}))\n" +
            "     AND (DATE(#{endDate}) IS NULL OR DATE(created_at) <= DATE(#{endDate}))\n" +
            "ORDER BY counter")
    List<PipelineInstance> allPipelineInstancesFor(@Param("name") String name,
                                                   @Param("startDate") ZonedDateTime startDate,
                                                   @Param("endDate") ZonedDateTime endDate);

    @Select("SELECT DISTINCT name FROM pipelines")
    @Results({
            @Result(property = "name", column = "name"),
    })
    List<Pipeline> allPipelines();

    @Select("  SELECT name, AVG(time_waiting_secs) avg_time_waiting_secs, AVG(total_time_secs - time_waiting_secs) avg_time_building_secs\n" +
            "    FROM pipelines\n" +
            "   WHERE DATE(created_at) >= DATE(#{startDate})\n" +
            "     AND DATE(created_at) <= DATE(#{endDate})\n" +
            "GROUP BY name\n" +
            "ORDER BY avg_time_waiting_secs DESC\n" +
            "   LIMIT #{limit}")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "avgWaitTimeSecs", column = "avg_time_waiting_secs"),
            @Result(property = "avgBuildTimeSecs", column = "avg_time_building_secs")
    })
    List<Pipeline> longestWaiting(@Param("startDate") ZonedDateTime startDate,
                                  @Param("endDate") ZonedDateTime endDate,
                                  @Param("limit") int limit);

    @ResultMap("PipelineInstance")
    @Select("  SELECT *\n" +
            "    FROM pipelines\n" +
            "   WHERE name = #{name}\n" +
            "     AND counter = #{counter}\n" +
            "     LIMIT 1;")
    PipelineInstance find(@Param("name") String name, @Param("counter") int counter);

    @Results(id = "PipelineInstanceWithWorkflow", value = {
            @Result(property = "name", column = "name"),
            @Result(property = "counter", column = "counter"),
            @Result(property = "totalTimeSecs", column = "total_time_secs"),
            @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "lastTransitionTime", column = "last_transition_time"),
            @Result(property = "result", column = "result"),
            @Result(property = "workflowId", column = "workflow_id")
    })
    @Select("<script>" +
            "SELECT p.*, pw.workflow_id" +
            "  FROM pipelines p" +
            "  JOIN " +
            "   (SELECT MAX(p.id) id, pw.workflow_id " +
            "    FROM pipelines p " +
            "      JOIN " +
            "    pipeline_workflows pw " +
            "      ON p.id=pw.pipeline_id " +
            "      AND pw.workflow_id IN " +
            "        (SELECT pw.workflow_id " +
            "          FROM pipeline_workflows pw " +
            "        JOIN " +
            "          (SELECT id FROM pipelines WHERE name=#{source}) p ON p.id=pw.pipeline_id) " +
            "      AND p.name IN " +
            "      <foreach item='item' index='index' collection='pipelines'" +
            "        open='(' separator=',' close=')'>" +
            "        #{item}" +
            "      </foreach>" +
            "       GROUP BY p.name, pw.workflow_id" +
            "     ) pw" +
            "     ON p.id = pw.id" +
            "</script>")
    List<PipelineInstance> allPipelineInstancesWithNameIn(@Param("source") String source, @Param("pipelines") List<String> pipelines);
}
