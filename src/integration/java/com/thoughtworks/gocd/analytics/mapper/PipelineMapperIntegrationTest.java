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

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PipelineMapperIntegrationTest {
    private TestDBConnectionManager manager;
    private PipelineMapper mapper;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        manager = new TestDBConnectionManager();
        mapper = manager.getSqlSession().getMapper(PipelineMapper.class);
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldConsiderPipelineNameToBeCaseInsensitive() {
        PipelineInstance pipelineNamedWithUpperCase = new PipelineInstance(1, "PIPELINE", 1, 1, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
        mapper.insert(pipelineNamedWithUpperCase);
        assertEquals(1, mapper.allPipelineInstancesFor(pipelineNamedWithUpperCase.getName(), null, ZonedDateTime.now()).size());
        Long pipelineId = mapper.allPipelineInstancesFor("PIPELINE", null, ZonedDateTime.now()).get(0).getId();

        assertEquals(pipelineId, mapper.findInstance("pipeline", pipelineNamedWithUpperCase.getCounter()).getId());
        assertEquals(pipelineId, mapper.findInstance("PIPELINE", pipelineNamedWithUpperCase.getCounter()).getId());
    }

    @Test
    public void updateShouldNotChangeCreatedAt() {
        ZonedDateTime createdAt = ZonedDateTime.now();
        PipelineInstance pipelineInstance = new PipelineInstance(1, "wubba_lubba_dub_dub", 1, 2, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
        pipelineInstance.setCreatedAt(createdAt);
        mapper.insert(pipelineInstance);
        PipelineInstance beforeUpdate = mapper.allPipelineInstancesFor(pipelineInstance.getName(), null, ZonedDateTime.now()).get(0);
        assertEquals(createdAt.toInstant().truncatedTo(SECONDS), beforeUpdate.getCreatedAt().toInstant().truncatedTo(SECONDS));

        pipelineInstance.setId(beforeUpdate.getId());
        pipelineInstance.setCreatedAt(ZonedDateTime.now());
        mapper.update(pipelineInstance);

        PipelineInstance afterUpdate = mapper.allPipelineInstancesFor(pipelineInstance.getName(), null, ZonedDateTime.now()).get(0);
        assertEquals(createdAt.toInstant().truncatedTo(SECONDS), afterUpdate.getCreatedAt().toInstant().truncatedTo(SECONDS));
    }

    @Test
    public void shouldAggregrateTotalTimeAndTimeWaitingWhenUpdating() {
        PipelineInstance pipelineInstance = new PipelineInstance(1, "wubba_lubba_dub_dub", 1, 2, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
        mapper.insert(pipelineInstance);
        List<PipelineInstance> beforeUpdate = mapper.allPipelineInstancesFor(pipelineInstance.getName(), null, ZonedDateTime.now());
        assertEquals(1, beforeUpdate.size());
        assertEquals(1, beforeUpdate.get(0).getTotalTimeSecs());
        assertEquals(2, beforeUpdate.get(0).getTimeWaitingSecs());

        pipelineInstance.setId(beforeUpdate.get(0).getId());

        mapper.update(pipelineInstance);

        List<PipelineInstance> afterUpdate = mapper.allPipelineInstancesFor(pipelineInstance.getName(), null, ZonedDateTime.now());
        assertEquals(1, afterUpdate.size());
        assertEquals(2, afterUpdate.get(0).getTotalTimeSecs());
        assertEquals(4, afterUpdate.get(0).getTimeWaitingSecs());

    }
}
