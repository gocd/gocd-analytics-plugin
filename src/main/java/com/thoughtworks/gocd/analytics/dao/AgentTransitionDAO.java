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

import com.thoughtworks.gocd.analytics.mapper.AgentTransitionsMapper;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.models.AgentUtilizationSummary;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;
import java.util.List;

public class AgentTransitionDAO {
    protected static AgentTransitionsMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(AgentTransitionsMapper.class);
    }

    public void insertTransition(SqlSession sqlSession, AgentTransition transition) {
        mapper(sqlSession).insert(transition);
    }

    public List<AgentTransition> findByUuid(SqlSession sqlSession, ZonedDateTime start, ZonedDateTime end, String agentUUID) {
        return mapper(sqlSession).findByUuid(start, end, agentUUID);
    }

    public void deleteTransitionsPriorTo(SqlSession sqlSession, ZonedDateTime priorToDate) {
        mapper(sqlSession).deleteTransitionsPriorTo(priorToDate);
    }

    public List<AgentUtilizationSummary> allWaitingFor(SqlSession sqlSession) {
        return mapper(sqlSession).allWaitingFor();
    }
}
