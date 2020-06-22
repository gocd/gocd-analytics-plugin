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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DBAccessTest {
    private DBAccess db;

    @Before
    public void before() {
        db = new DBAccess();
    }

    @After
    public void after() throws SQLException, InterruptedException {
        if (db.database() != null) {
            db.database().clean();
            db.database().close();
        }
    }

    @Test
    public void shouldNotBeAbleToConnectToDbWithoutConfig() {
        assertThat(db.canConnectToDB(), is(false));
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

        assertThat(db.canConnectToDB(), is(false));
    }

    @Test
    public void shouldBeAbleToConnectWithValidConfig() throws SQLException, InterruptedException {
        db.initialize(TestDBConnectionManager.connectionSettings());
        assertThat(db.canConnectToDB(), is(true));
    }
}
