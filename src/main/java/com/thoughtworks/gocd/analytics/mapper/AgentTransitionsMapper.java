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

import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.models.AgentUtilizationSummary;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface AgentTransitionsMapper {
    @Insert("INSERT INTO agent_transitions(uuid," +
            "agent_config_state," +
            "agent_state," +
            "build_state," +
            "transition_time) VALUES " +
            "(#{uuid}," +
            "#{agentConfigState}," +
            "#{agentState}," +
            "#{buildState}," +
            "#{transitionTime})")
    void insert(AgentTransition agentTransition);

    @Select("SELECT *\n" +
            "  FROM agent_transitions\n" +
            " WHERE uuid = #{agentUUID}\n" +
            "   AND (DATE(#{startDate}) IS NULL OR DATE(transition_time) >= DATE(#{startDate}))\n" +
            "   AND (DATE(#{endDate}) IS NULL OR DATE(transition_time) <= DATE(#{endDate}))\n" +
            " ORDER BY transition_time ASC")
    @Results(id = "AgentTransition", value = {
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "agentConfigState", column = "agent_config_state"),
            @Result(property = "agentState", column = "agent_state"),
            @Result(property = "buildState", column = "build_state"),
            @Result(property = "transitionTime", column = "transition_time")
    })
    List<AgentTransition> findByUuid(@Param("startDate") ZonedDateTime startDate,
                                     @Param("endDate") ZonedDateTime endDate,
                                     @Param("agentUUID") String agentUUID);

    @Delete("DELETE\n" +
            "  FROM agent_transitions\n" +
            " WHERE transition_time AT TIME ZONE 'UTC' < #{transition_time} AT TIME ZONE 'UTC';")
    void deleteTransitionsPriorTo(@Param("transition_time") ZonedDateTime transitionTime);

    @Results(id = "AllWaitingFor", value = {
        @Result(property = "utilizationDate", column = "utilization_date"),
        @Result(property = "idleDurationSecs", column = "idle_duration_secs")
    })
    @Select("WITH MergedData AS (\n"
        + "  SELECT\n"
        + "    DATE(utilization_date) AS date_only,\n"
        + "    SUM(idle_duration_secs) AS total_time\n"
        + "  FROM agent_utilization\n"
        + "  GROUP BY DATE(utilization_date)\n"
        + ")\n"
        + "SELECT\n"
        + "  date_only AS utilization_date,\n"
        + "  total_time AS idle_duration_secs\n"
        + "FROM MergedData;")
    List<AgentUtilizationSummary> allWaitingFor();
}
