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

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals("https://build.go.cd/go", pluginSettings.getDbHost());
        assertEquals("bob", pluginSettings.getDbUsername());
        assertEquals("p@ssw0rd", pluginSettings.getDbPassword());
        assertEquals("eek", pluginSettings.getDbName());
        assertEquals(5432, pluginSettings.getDbPort());
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

        assertEquals("https://build.go.cd/go", pluginSettings.getDbHost());
        assertEquals("bob", pluginSettings.getDbUsername());
        assertEquals("p@ssw0rd", pluginSettings.getDbPassword());
        assertEquals("eek", pluginSettings.getDbName());
        assertEquals(5432, pluginSettings.getDbPort());
        assertTrue(pluginSettings.useSsl());
        assertEquals("verify_full", pluginSettings.getSslMode());
        assertEquals("root_cert", pluginSettings.getRootCert());
        assertEquals("client_cert", pluginSettings.getClientCert());
        assertEquals("client_key", pluginSettings.getClientKey());
        assertEquals("client_pkcs8_key", pluginSettings.getClientPKCS8Key());
    }

    @Test
    public void dbPortShouldDefaultTo5432() {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"host\": \"https://build.go.cd/go\", " +
                "\"username\": \"bob\", " +
                "\"password\": \"p@ssw0rd\", " +
                "\"name\": \"eek\" " +
                "}");
        assertEquals(5432, pluginSettings.getDbPort());
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

        assertEquals("localhost", pluginSettings.getDbHost());
        assertEquals("morty", pluginSettings.getDbUsername());
        assertEquals("badger", pluginSettings.getDbPassword());
        assertEquals("cruise", pluginSettings.getDbName());
        assertEquals(5432, pluginSettings.getDbPort());
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
        assertEquals(10, pluginSettings.getMaxConnectionsActive());
        assertEquals(8, pluginSettings.getMaxConnectionsIdle());
        assertEquals(-1, pluginSettings.getMaxConnectionWaitTime());
        assertEquals(LocalTime.parse("00:00", DateTimeFormatter.ofPattern("HH:mm")), pluginSettings.getPeriodicCleanupTime());
    }

    @Test
    public void shouldDeserializeAdvancedConnectionSettingsFromJSON() {
        PluginSettings pluginSettings = PluginSettings.fromJSON(jsonForAdvancedConnectionSettings("invalid", "42", "-20", "20:00"));
        assertEquals(10, pluginSettings.getMaxConnectionsActive());
        assertEquals(42, pluginSettings.getMaxConnectionsIdle());
        assertEquals(-20, pluginSettings.getMaxConnectionWaitTime());
        assertEquals(LocalTime.parse("20:00", DateTimeFormatter.ofPattern("HH:mm")), pluginSettings.getPeriodicCleanupTime());
    }

    private String jsonForAdvancedConnectionSettings(final String active, final String idle, final String waitTime, final String periodicCleanUpTime) {
        return String.format("{\"max_connections_active\": \"%s\", \"max_connections_idle\": \"%s\" ,\"max_connection_wait_time\": \"%s\", \"periodic_cleanup_time\": \"%s\"}", active, idle, waitTime, periodicCleanUpTime);
    }
}
