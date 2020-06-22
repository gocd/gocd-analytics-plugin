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

import com.thoughtworks.gocd.analytics.models.MaterialRevision;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface MaterialRevisionMapper {
    @Insert("INSERT INTO material_revisions " +
            "(fingerprint," +
            "revision," +
            "build_schedule_time) VALUES " +
            "(#{fingerprint}," +
            "#{revision}," +
            "#{buildScheduleTime})")
    @SelectKey(statement="SELECT lastval()", keyProperty="id", keyColumn ="id", before=false, resultType=long.class)
    void insert(MaterialRevision materialRevision);

    @Results(id = "MaterialRevision", value = {
            @Result(property = "fingerprint", column = "fingerprint"),
            @Result(property = "revision", column = "revision"),
            @Result(property = "buildScheduleTime", column = "build_schedule_time"),
    })
    @Select("SELECT * FROM material_revisions\n" +
            "  WHERE fingerprint = #{fingerprint}\n" +
            "  AND revision = #{revision}\n" +
            "  LIMIT 1;")
    MaterialRevision materialRevisionFor(@Param("fingerprint") String fingerprint, @Param("revision") String revision);

    @Results(id = "AllMaterialRevision", value = {
            @Result(property = "fingerprint", column = "fingerprint"),
            @Result(property = "revision", column = "revision"),
            @Result(property = "buildScheduleTime", column = "build_schedule_time"),
    })
    @Select("SELECT * FROM material_revisions;")
    List<MaterialRevision> all();
}
