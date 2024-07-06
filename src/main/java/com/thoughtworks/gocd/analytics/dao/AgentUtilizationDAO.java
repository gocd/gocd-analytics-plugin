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


import com.thoughtworks.gocd.analytics.mapper.AgentUtilizationMapper;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;
import java.util.List;

public class AgentUtilizationDAO {

    public void insert(SqlSession sqlSession, AgentUtilization agentUtilization) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        mapper.insert(agentUtilization);
    }

    public void update(SqlSession sqlSession, AgentUtilization agentUtilization) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        mapper.update(agentUtilization);
    }

    public AgentUtilization findUtilization(SqlSession sqlSession, String uuid, ZonedDateTime utilizationDate) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        return mapper.findInstance(uuid, utilizationDate);
    }

    public AgentUtilization findLatestUtilization(SqlSession sqlSession, String uuid) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        return mapper.findLatestUtilization(uuid);
    }

    public List<AgentUtilization> all(SqlSession sqlSession, String uuid) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        return mapper.allAgentUtilization(uuid);
    }

    public List<AgentUtilization> highestUtilization(SqlSession sqlSession, String startDate,
        String endDate, int limit) {
        AgentUtilizationMapper mapper = mapper(sqlSession);

        return mapper.highestUtilization(startDate, endDate, limit);
    }

    public void deleteUtilizationPriorTo(SqlSession sqlSession, ZonedDateTime utilizationDate) {
        mapper(sqlSession).deleteUtilizationPriorTo(utilizationDate);
    }

    private AgentUtilizationMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(AgentUtilizationMapper.class);
    }
}
