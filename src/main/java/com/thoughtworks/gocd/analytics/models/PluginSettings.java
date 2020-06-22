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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginSettings {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("host")
    private String dbHost;
    @Expose
    @SerializedName("username")
    private String dbUsername;
    @Expose
    @SerializedName("password")
    private String dbPassword;
    @Expose
    @SerializedName("name")
    private String dbName;
    @Expose
    @SerializedName("port")
    private String dbPort;
    @Expose
    @SerializedName("use_ssl")
    private boolean useSsl = false;
    @Expose
    @SerializedName("ssl_mode")
    private String sslMode;
    @Expose
    @SerializedName("root_cert")
    private String rootCert;
    @Expose
    @SerializedName("client_cert")
    private String clientCert;
    @Expose
    @SerializedName("client_key")
    private String clientKey;
    @Expose
    @SerializedName("client_pkcs8_key")
    private String clientPKCS8Key;
    @Expose
    @SerializedName("max_connections_active")
    private String maxActiveConnections;
    @Expose
    @SerializedName("max_connections_idle")
    private String maxIdleConnections;
    @Expose
    @SerializedName("max_connection_wait_time")
    private String maxConnectionWaitTime;
    @Expose
    @SerializedName("periodic_cleanup_time")
    private String periodicCleanupTime;

    public PluginSettings() {
    }

    public PluginSettings(String dbHost, String dbPort, String dbUsername, String dbPassword, String dbName) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
    }

    public static PluginSettings fromJSON(String json) {
        return GSON.fromJson(json, PluginSettings.class);
    }

    public static PluginSettings fromValidateSettingsJSON(String json) {
        Map<String, Map<String, String>> settings = deserializeValidateSettingsJson(json).get("plugin-settings");

        Map<String, String> pluginSettings = settings.entrySet().stream()
                .filter(e -> !(e.getValue().isEmpty() || e.getValue() == null))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get("value")));

        return fromJSON(GSON.toJson(pluginSettings));
    }

    public static Map<String, Map<String, Map<String, String>>> deserializeValidateSettingsJson(String json) {
        return GSON.fromJson(json, new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
        }.getType());
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbUseSsl() {
        return null;
    }

    public int getDbPort() {
        return toInteger(dbPort, 5432);
    }

    public boolean useSsl() {
        return useSsl;
    }

    public String getSslMode() {
        return sslMode;
    }

    public String getRootCert() {
        return rootCert;
    }

    public String getClientCert() {
        return clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getClientPKCS8Key() {
        return clientPKCS8Key;
    }

    public int getMaxConnectionsActive() {
        return toInteger(maxActiveConnections, 10);
    }

    public int getMaxConnectionsIdle() {
        return toInteger(maxIdleConnections, 8);
    }

    public int getMaxConnectionWaitTime() {
        return toInteger(maxConnectionWaitTime, -1);
    }

    public LocalTime getPeriodicCleanupTime() {
        String time = StringUtils.isNotEmpty(periodicCleanupTime) ? periodicCleanupTime : "00:00";

        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isConfigured() {
        return StringUtils.isNotEmpty(dbUsername) && StringUtils.isNotEmpty(dbName) && StringUtils.isNotEmpty(dbHost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginSettings)) return false;
        PluginSettings that = (PluginSettings) o;
        return useSsl == that.useSsl &&
                Objects.equals(getDbHost(), that.getDbHost()) &&
                Objects.equals(getDbUsername(), that.getDbUsername()) &&
                Objects.equals(getDbPassword(), that.getDbPassword()) &&
                Objects.equals(getDbName(), that.getDbName()) &&
                Objects.equals(getDbPort(), that.getDbPort()) &&
                Objects.equals(getSslMode(), that.getSslMode()) &&
                Objects.equals(getRootCert(), that.getRootCert()) &&
                Objects.equals(getClientCert(), that.getClientCert()) &&
                Objects.equals(getClientKey(), that.getClientKey()) &&
                Objects.equals(getClientPKCS8Key(), that.getClientPKCS8Key()) &&
                Objects.equals(maxActiveConnections, that.maxActiveConnections) &&
                Objects.equals(maxIdleConnections, that.maxIdleConnections) &&
                Objects.equals(getMaxConnectionWaitTime(), that.getMaxConnectionWaitTime()) &&
                Objects.equals(getPeriodicCleanupTime(), that.getPeriodicCleanupTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDbHost(), getDbUsername(), getDbPassword(), getDbName(), getDbPort(), useSsl, getSslMode(), getRootCert(), getClientCert(), getClientKey(), getClientPKCS8Key(), maxActiveConnections, maxIdleConnections, getMaxConnectionWaitTime(), getPeriodicCleanupTime());
    }

    private int toInteger(String valueAsString, int defaultValue) {
        if (StringUtils.isBlank(valueAsString)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueAsString);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
