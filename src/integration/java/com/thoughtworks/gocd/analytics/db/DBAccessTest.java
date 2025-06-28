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

package com.thoughtworks.gocd.analytics.db;

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DBAccessTest {
    private DBAccess db;

    @BeforeEach
    public void before() {
        db = new DBAccess();
    }

    @AfterEach
    public void after() throws SQLException, InterruptedException {
        if (db.database() != null) {
            db.database().tryClean();
            db.database().close();
        }
    }

    @Test
    public void shouldNotBeAbleToConnectToDbWithoutConfig() {
        assertEquals(false, db.canConnectToDB());
    }

    @Test
    public void shouldNotBeAbleToConnectWithInvalidConfig() throws SQLException, InterruptedException {
        PluginSettings settings = PluginSettings.fromJSON("{" +
                "\"host\": \"localhost\", " +
                "\"port\": \"5432\", " +
                "\"username\": \"rick -- " + ThreadLocalRandom.current().nextInt(0, 2048) + "!!\", " +
                "\"password\": \"sanchez\", " +
                "\"name\": \"blah\" " +
                "}");
        try {
            db.initialize(settings);
        } catch (Exception ignored) {
        }

        assertEquals(false, db.canConnectToDB());
    }

    @Test
    public void shouldBeAbleToConnectWithValidConfig() throws SQLException, InterruptedException {
        db.initialize(TestDBConnectionManager.connectionSettings());
        assertEquals(true, db.canConnectToDB());
    }
}
