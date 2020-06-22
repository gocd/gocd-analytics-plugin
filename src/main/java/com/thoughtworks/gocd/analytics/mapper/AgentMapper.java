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


import com.thoughtworks.gocd.analytics.models.Agent;
import org.apache.ibatis.annotations.*;

public interface AgentMapper {
    @Insert("INSERT INTO agents(uuid," +
            "host_name," +
            "is_elastic," +
            "ip_address," +
            "operating_system," +
            "free_space," +
            "config_state," +
            "created_at) VALUES " +
            "(#{uuid}," +
            "#{hostName}," +
            "#{isElastic}," +
            "#{ipAddress}," +
            "#{operatingSystem}," +
            "#{freeSpace}," +
            "#{configState}," +
            "NOW())")
    void insert(Agent agent);

    @Results(id = "Agent", value = {
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "hostName", column = "host_name"),
            @Result(property = "isElastic", column = "is_elastic"),
            @Result(property = "ipAddress", column = "ip_address"),
            @Result(property = "operatingSystem", column = "operating_system"),
            @Result(property = "freeSpace", column = "free_space"),
            @Result(property = "configState", column = "config_state"),
    })
    @Select("SELECT * FROM agents WHERE uuid = #{uuid}")
    Agent findInstance(@Param("uuid") String uuid);

    @Update("UPDATE agents\n" +
            "   SET host_name = #{hostName},\n" +
            "       is_elastic = #{isElastic},\n" +
            "       ip_address = #{ipAddress},\n" +
            "       operating_system = #{operatingSystem},\n" +
            "       free_space = #{freeSpace},\n" +
            "       config_state = #{configState}\n" +
            " WHERE uuid = #{uuid}")
    void update(Agent agent);
}
