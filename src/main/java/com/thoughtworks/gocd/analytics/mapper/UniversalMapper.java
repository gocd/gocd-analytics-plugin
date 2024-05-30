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


import com.thoughtworks.gocd.analytics.models.UniversalSummary;
import java.util.List;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface UniversalMapper {

//    @Results(id = "PipelineInstance", value = {
//        @Result(property = "name", column = "name"),
//        @Result(property = "counter", column = "counter"),
//        @Result(property = "totalTimeSecs", column = "total_time_secs"),
//        @Result(property = "timeWaitingSecs", column = "time_waiting_secs"),
//        @Result(property = "createdAt", column = "created_at"),
//        @Result(property = "lastTransitionTime", column = "last_transition_time"),
//        @Result(property = "result", column = "result")
//    })
//    @Select("SELECT * FROM pipelines WHERE name = #{name} AND counter = #{counter} LIMIT 1")
//    PipelineInstance findInstance(@Param("name") String name, @Param("counter") int counter);

//    @ResultMap("UniversalSummary")


    @Results(id = "universal_summary", value = {
        @Result(property = "fullTableName", column = "full_table_name"),
        @Result(property = "totalSize", column = "total_size"),
    })
    @Select("SELECT schemaname || '.' || tablename AS full_table_name,\n"
        + "       total_bytes AS total_size\n"
        + "FROM (\n"
        + "  SELECT schemaname, tablename, pg_total_relation_size(schemaname || '.' || tablename) AS total_bytes\n"
        + "  FROM pg_tables\n"
        + "  WHERE schemaname = 'public' -- Replace with your actual schema name\n"
        + ") AS table_sizes\n"
        + "ORDER BY total_bytes DESC;")
    List<UniversalSummary> getAllDB();
}
