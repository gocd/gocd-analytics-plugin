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

import com.thoughtworks.gocd.analytics.mapper.MaterialRevisionMapper;
import com.thoughtworks.gocd.analytics.models.MaterialRevision;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class MaterialRevisionDAO {

    public void insert(SqlSession sqlSession, MaterialRevision materialRevision) {
        MaterialRevision revision = mapper(sqlSession).materialRevisionFor(materialRevision.getFingerprint(), materialRevision.getRevision());

        if (revision == null) {
            mapper(sqlSession).insert(materialRevision);
        } else {
            materialRevision.setId(revision.getId());
        }
    }

    public MaterialRevision find(SqlSession sqlSession, String fingerprint, String revision) {
        return mapper(sqlSession).materialRevisionFor(fingerprint, revision);
    }

    public List<MaterialRevision> all(SqlSession sqlSession) {
        return mapper(sqlSession).all();
    }

    protected static MaterialRevisionMapper mapper(SqlSession sqlSession) {
        return sqlSession.getMapper(MaterialRevisionMapper.class);
    }
}
