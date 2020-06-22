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

package com.thoughtworks.gocd.analytics.cleanup;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.db.TransactionAware;
import org.apache.ibatis.session.SqlSession;

import java.time.ZonedDateTime;

public class DataPurger extends TransactionAware {
    private final Logger LOG = Logger.getLoggerFor(DataPurger.class);
    private final JobDAO jobDAO;
    private final StageDAO stageDAO;
    private final AgentUtilizationDAO agentUtilizationDAO;
    private final AgentTransitionDAO agentTransitionDAO;

    public DataPurger(SessionFactory sessionFactory) {
        this(new JobDAO(), new StageDAO(), new AgentUtilizationDAO(), new AgentTransitionDAO(), sessionFactory);
    }

    protected DataPurger(JobDAO jobDAO, StageDAO stageDAO, AgentUtilizationDAO agentUtilizationDAO, AgentTransitionDAO agentTransitionDAO, SessionFactory sessionFactory) {
        super(sessionFactory);
        this.jobDAO = jobDAO;
        this.stageDAO = stageDAO;
        this.agentUtilizationDAO = agentUtilizationDAO;
        this.agentTransitionDAO = agentTransitionDAO;
    }

    public void purge(ZonedDateTime dataRetentionDate) {
        LOG.info("[Data-Purge] Start purging Jobs, Stages and AgentUtilization data prior to: '{}'", dataRetentionDate);
        purgeJobRuns(dataRetentionDate);
        purgeStageRuns(dataRetentionDate);
        purgeAgentUtilizationRuns(dataRetentionDate);
        purgeAgentTransitions(dataRetentionDate);
        LOG.info("[Data-Purge] Done purging Jobs, Stages and AgentUtilization data prior to: '{}'", dataRetentionDate);
    }

    private void purgeJobRuns(ZonedDateTime priorToDate) {
        try {
            doInTransaction(new Operation<Boolean>() {
                @Override
                public Boolean execute(SqlSession sqlSession) {
                    LOG.info("[Data-Purge] Start purging Jobs.");
                    jobDAO.deleteJobRunsPriorTo(sqlSession, priorToDate);
                    LOG.info("[Data-Purge] Done purging Jobs.");
                    return true;
                }
            });
        } catch (Exception e) {
            LOG.error("[Data-Purge] Error deleting jobs prior to: '{}'", priorToDate, e);
        }
    }

    private void purgeStageRuns(ZonedDateTime priorToDate) {
        try {
            doInTransaction(new Operation<Boolean>() {
                @Override
                public Boolean execute(SqlSession sqlSession) {
                    LOG.info("[Data-Purge] Start purging Stages.");
                    stageDAO.deleteStageRunsPriorTo(sqlSession, priorToDate);
                    LOG.info("[Data-Purge] Done purging Stages.");
                    return true;
                }
            });
        } catch (Exception e) {
            LOG.error("[Data-Purge] Error deleting stages prior to: '{}'", priorToDate, e);
        }
    }

    private void purgeAgentUtilizationRuns(ZonedDateTime priorToDate) {
        try {
            doInTransaction(new Operation<Boolean>() {
                @Override
                public Boolean execute(SqlSession sqlSession) {
                    LOG.info("[Data-Purge] Start purging AgentUtilization.");
                    agentUtilizationDAO.deleteUtilizationPriorTo(sqlSession, priorToDate);
                    LOG.info("[Data-Purge] Done purging AgentUtilization.");
                    return true;
                }
            });
        } catch (Exception e) {
            LOG.error("[Data-Purge] Error deleting agent utilization prior to: '{}'", priorToDate, e);
        }
    }

    private void purgeAgentTransitions(ZonedDateTime priorToDate) {
        try {
            doInTransaction(new Operation<Boolean>() {
                @Override
                public Boolean execute(SqlSession sqlSession) {
                    LOG.info("[Data-Purge] Start purging AgentTransitions.");
                    agentTransitionDAO.deleteTransitionsPriorTo(sqlSession, priorToDate);
                    LOG.info("[Data-Purge] Done purging AgentTransitions.");
                    return true;
                }
            });
        } catch (Exception e) {
            LOG.error("[Data-Purge] Error deleting agent Transitions prior to: '{}'", priorToDate, e);
        }
    }
}
