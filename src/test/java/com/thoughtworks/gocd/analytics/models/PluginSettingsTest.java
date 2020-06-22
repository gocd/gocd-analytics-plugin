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

package com.thoughtworks.gocd.analytics.models;

import org.junit.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PluginSettingsTest {
    @Test
    public void shouldDeserializeFromJSON() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"host\": \"https://build.go.cd/go\", " +
                "\"username\": \"bob\", " +
                "\"password\": \"p@ssw0rd\", " +
                "\"port\": \"5432\", " +
                "\"name\": \"eek\" " +
                "}");

        assertThat(pluginSettings.getDbHost(), is("https://build.go.cd/go"));
        assertThat(pluginSettings.getDbUsername(), is("bob"));
        assertThat(pluginSettings.getDbPassword(), is("p@ssw0rd"));
        assertThat(pluginSettings.getDbName(), is("eek"));
        assertThat(pluginSettings.getDbPort(), is(5432));
    }

    @Test
    public void shouldDeserializeFromJSONWithSSLSettings() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"host\": \"https://build.go.cd/go\", " +
                "\"username\": \"bob\", " +
                "\"password\": \"p@ssw0rd\", " +
                "\"port\": \"5432\", " +
                "\"name\": \"eek\"," +
                "\"use_ssl\": \"true\"," +
                "\"ssl_mode\": \"verify_full\"," +
                "\"root_cert\": \"root_cert\"," +
                "\"client_cert\": \"client_cert\"," +
                "\"client_key\": \"client_key\"," +
                "\"client_pkcs8_key\": \"client_pkcs8_key\"" +
                "}");

        assertThat(pluginSettings.getDbHost(), is("https://build.go.cd/go"));
        assertThat(pluginSettings.getDbUsername(), is("bob"));
        assertThat(pluginSettings.getDbPassword(), is("p@ssw0rd"));
        assertThat(pluginSettings.getDbName(), is("eek"));
        assertThat(pluginSettings.getDbPort(), is(5432));
        assertTrue(pluginSettings.useSsl());
        assertThat(pluginSettings.getSslMode(), is("verify_full"));
        assertThat(pluginSettings.getRootCert(), is("root_cert"));
        assertThat(pluginSettings.getClientCert(), is("client_cert"));
        assertThat(pluginSettings.getClientKey(), is("client_key"));
        assertThat(pluginSettings.getClientPKCS8Key(), is("client_pkcs8_key"));
    }

    @Test
    public void dbPortShouldDefaultTo5432() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"host\": \"https://build.go.cd/go\", " +
                "\"username\": \"bob\", " +
                "\"password\": \"p@ssw0rd\", " +
                "\"name\": \"eek\" " +
                "}");
        assertThat(pluginSettings.getDbPort(), is(5432));
    }

    @Test
    public void shouldDeserializeFromValidateRequestJSON() {
        String json = "{\n" +
                "  \"plugin-settings\":{\n" +
                "    \"password\":{\n" +
                "      \"value\":\"badger\"\n" +
                "    },\n" +
                "      \"port\":{},\n" +
                "      \"host\":{\n" +
                "        \"value\":\"localhost\"\n" +
                "      },\n" +
                "      \"name\":{\n" +
                "        \"value\":\"cruise\"\n" +
                "      },\n" +
                "      \"connection\":{},\n" +
                "      \"username\":{\n" +
                "        \"value\":\"morty\"\n" +
                "      }\n" +
                "  }\n" +
                "}\n";

        PluginSettings pluginSettings = PluginSettings.fromValidateSettingsJSON(json);

        assertThat(pluginSettings.getDbHost(), is("localhost"));
        assertThat(pluginSettings.getDbUsername(), is("morty"));
        assertThat(pluginSettings.getDbPassword(), is("badger"));
        assertThat(pluginSettings.getDbName(), is("cruise"));
        assertThat(pluginSettings.getDbPort(), is(5432));
    }

    @Test
    public void shouldBeConfiguredIfMandatoryFieldsAreSpecified() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"host\": \"https://build.go.cd/go\", " +
                "\"username\": \"bob\", " +
                "\"name\": \"eek\" " +
                "}");

        assertTrue(pluginSettings.isConfigured());
    }

    @Test
    public void shouldGetDefaultValuesForAdvancedConnectionSettingsIfNotProvided() {
        PluginSettings pluginSettings = PluginSettings.fromJSON(jsonForAdvancedConnectionSettings("", "", "", ""));
        assertThat(pluginSettings.getMaxConnectionsActive(), is(10));
        assertThat(pluginSettings.getMaxConnectionsIdle(), is(8));
        assertThat(pluginSettings.getMaxConnectionWaitTime(), is(-1));
        assertThat(pluginSettings.getPeriodicCleanupTime(), is(LocalTime.parse("00:00", DateTimeFormatter.ofPattern("HH:mm"))));
    }

    @Test
    public void shouldDeserializeAdvancedConnectionSettingsFromJSON() {
        PluginSettings pluginSettings = PluginSettings.fromJSON(jsonForAdvancedConnectionSettings("invalid", "42", "-20", "20:00"));
        assertThat(pluginSettings.getMaxConnectionsActive(), is(10));
        assertThat(pluginSettings.getMaxConnectionsIdle(), is(42));
        assertThat(pluginSettings.getMaxConnectionWaitTime(), is(-20));
        assertThat(pluginSettings.getPeriodicCleanupTime(), is(LocalTime.parse("20:00", DateTimeFormatter.ofPattern("HH:mm"))));
    }

    private String jsonForAdvancedConnectionSettings(final String active, final String idle, final String waitTime, final String periodicCleanUpTime) {
        return String.format("{\"max_connections_active\": \"%s\", \"max_connections_idle\": \"%s\" ,\"max_connection_wait_time\": \"%s\", \"periodic_cleanup_time\": \"%s\"}", active, idle, waitTime, periodicCleanUpTime);
    }
}
