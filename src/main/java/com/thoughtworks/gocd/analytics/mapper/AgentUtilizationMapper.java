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

import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface AgentUtilizationMapper {

    @Insert("INSERT INTO agent_utilization(uuid," +
            "idle_duration_secs," +
            "building_duration_secs," +
            "cancelled_duration_secs," +
            "lost_contact_duration_secs," +
            "unknown_duration_secs," +
            "last_known_state," +
            "utilization_date," +
            "last_transition_time) values " +
            "(#{uuid}," +
            "#{idleDurationSecs}," +
            "#{buildingDurationSecs}," +
            "#{cancelledDurationSecs}," +
            "#{lostContactDurationSecs}," +
            "#{unknownDurationSecs}," +
            "#{lastKnownState}," +
            "#{utilizationDate}," +
            "#{lastTransitionTime})")
    void insert(AgentUtilization agentUtilization);

    @Results(id = "AgentUtilization", value = {
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "idleDurationSecs", column = "idle_duration_secs"),
            @Result(property = "buildingDurationSecs", column = "building_duration_secs"),
            @Result(property = "cancelledDurationSecs", column = "cancelled_duration_secs"),
            @Result(property = "lostContactDurationSecs", column = "lost_contact_duration_secs"),
            @Result(property = "unknownDurationSecs", column = "unknown_duration_secs"),
            @Result(property = "lastKnownState", column = "last_known_state"),
            @Result(property = "utilizationDate", column = "utilization_date"),
            @Result(property = "lastTransitionTime", column = "last_transition_time")
    })
    @Select("SELECT * FROM agent_utilization\n" +
            " WHERE uuid = #{uuid}\n" +
            "   AND DATE(utilization_date AT TIME ZONE 'UTC') = DATE(#{utilizationDate} AT TIME ZONE 'UTC')\n" +
            " LIMIT 1")
    AgentUtilization findInstance(@Param("uuid") String uuid, @Param("utilizationDate") ZonedDateTime utilizationDate);

    @Update("UPDATE agent_utilization\n" +
            "   SET idle_duration_secs = #{idleDurationSecs},\n" +
            "       building_duration_secs = #{buildingDurationSecs},\n" +
            "       cancelled_duration_secs = #{cancelledDurationSecs},\n" +
            "       lost_contact_duration_secs = #{lostContactDurationSecs},\n" +
            "       unknown_duration_secs = #{unknownDurationSecs},\n" +
            "       last_known_state = #{lastKnownState},\n" +
            "       utilization_date = #{utilizationDate},\n" +
            "       last_transition_time = #{lastTransitionTime}\n" +
            " WHERE id = #{id}")
    void update(AgentUtilization agentUtilization);

    @Results(id = "LatestAgentUtilization", value = {
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "idleDurationSecs", column = "idle_duration_secs"),
            @Result(property = "buildingDurationSecs", column = "building_duration_secs"),
            @Result(property = "cancelledDurationSecs", column = "cancelled_duration_secs"),
            @Result(property = "lostContactDurationSecs", column = "lost_contact_duration_secs"),
            @Result(property = "unknownDurationSecs", column = "unknown_duration_secs"),
            @Result(property = "lastKnownState", column = "last_known_state"),
            @Result(property = "utilizationDate", column = "utilization_date"),
            @Result(property = "lastTransitionTime", column = "last_transition_time")
    })
    @Select("SELECT * FROM agent_utilization WHERE uuid = #{uuid} ORDER BY utilization_date DESC LIMIT 1")
    AgentUtilization findLatestUtilization(@Param("uuid") String uuid);

    @Select("SELECT * FROM agent_utilization WHERE uuid = #{uuid}")
    @Results(id = "AllAgentUtilization", value = {
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "idleDurationSecs", column = "idle_duration_secs"),
            @Result(property = "buildingDurationSecs", column = "building_duration_secs"),
            @Result(property = "cancelledDurationSecs", column = "cancelled_duration_secs"),
            @Result(property = "lostContactDurationSecs", column = "lost_contact_duration_secs"),
            @Result(property = "unknownDurationSecs", column = "unknown_duration_secs"),
            @Result(property = "lastKnownState", column = "last_known_state"),
            @Result(property = "utilizationDate", column = "utilization_date"),
            @Result(property = "lastTransitionTime", column = "last_transition_time")
    })
    List<AgentUtilization> allAgentUtilization(@Param("uuid") String uuid);

    @Select("SELECT agent.uuid, agent.host_name, utilization.idle_duration_secs, utilization.building_duration_secs\n" +
            "  FROM agents agent" +
            "  JOIN (SELECT agent_uuid from jobs group by agent_uuid HAVING count(*) >= 3) as jobs ON agent.uuid=jobs.agent_uuid\n" +
            "  JOIN (SELECT uuid,\n" +
            "               avg(idle_duration_secs) as idle_duration_secs,\n" +
            "               AVG(COALESCE(building_duration_secs,0) + COALESCE(cancelled_duration_secs,0)) as building_duration_secs\n" +
            "          FROM agent_utilization u\n" +
            "         WHERE DATE(utilization_date) >= DATE(#{startDate})\n" +
            "           AND DATE(utilization_date) <= DATE(#{endDate})\n" +
            "         GROUP BY u.uuid) AS utilization\n" +
            "  ON agent.uuid = utilization.uuid\n" +
            "  ORDER BY utilization.building_duration_secs DESC\n" +
            " LIMIT #{limit}")
    @Results({
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "agentHostName", column = "host_name"),
            @Result(property = "idleDurationSecs", column = "idle_duration_secs"),
            @Result(property = "buildingDurationSecs", column = "building_duration_secs"),
    })
    List<AgentUtilization> highestUtilization(@Param("startDate") ZonedDateTime startDate,
                                              @Param("endDate") ZonedDateTime endDate,
                                              @Param("limit") int limit);


    @Delete("DELETE FROM agent_utilization\n" +
            "  WHERE utilization_date AT TIME ZONE 'UTC' < #{utilization_date} AT TIME ZONE 'UTC';")
    void deleteUtilizationPriorTo(@Param("utilization_date") ZonedDateTime utilizationDate);
}
