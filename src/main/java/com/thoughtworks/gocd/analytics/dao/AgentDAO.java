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

import com.thoughtworks.gocd.analytics.mapper.AgentMapper;
import com.thoughtworks.gocd.analytics.models.Agent;
import org.apache.ibatis.session.SqlSession;

public class AgentDAO {
    public void updateOrInsert(SqlSession sqlSession, Agent agent) {
        AgentMapper mapper = sqlSession.getMapper(AgentMapper.class);
        Agent instance = mapper.findInstance(agent.getUuid());
        if (instance != null) {
            mapper.update(agent);
        } else {
            mapper.insert(agent);
        }
    }

    public Agent findByUuid(SqlSession sqlSession, String uuid) {
        AgentMapper mapper = sqlSession.getMapper(AgentMapper.class);

        return mapper.findInstance(uuid);
    }
}
