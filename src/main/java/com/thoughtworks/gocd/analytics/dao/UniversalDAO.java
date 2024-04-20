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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.mapper.PipelineMapper;
import com.thoughtworks.gocd.analytics.mapper.UniversalMapper;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.PipelineStateSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummaryTwo;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class UniversalDAO {
    public static final Logger LOG = Logger.getLoggerFor(UniversalDAO.class);

    public List<PipelineInstance> getAllDB(SqlSession sqlSession) {
        return mapper(sqlSession).getAllDB();
    }

    private UniversalMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(UniversalMapper.class);
    }
}
