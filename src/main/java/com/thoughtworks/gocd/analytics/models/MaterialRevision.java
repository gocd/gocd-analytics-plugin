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

import com.google.common.base.Objects;
import com.thoughtworks.gocd.analytics.db.PersistentObject;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;

public class MaterialRevision extends PersistentObject {
    private String fingerprint;
    private String revision;
    private String type;
    private ZonedDateTime buildScheduleTime;
    private final String DEPENDENCY_MATERIAL_TYPE = "pipeline";

    public MaterialRevision() {
    }

    public MaterialRevision(String fingerprint, String revision, String type, ZonedDateTime buildScheduleTime) {
        this.fingerprint = fingerprint;
        this.revision = revision;
        this.type = type;
        this.buildScheduleTime = buildScheduleTime;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getRevision() {
        return revision;
    }

    public ZonedDateTime getBuildScheduleTime() {
        return buildScheduleTime;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public void setBuildScheduleTime(ZonedDateTime buildScheduleTime) {
        this.buildScheduleTime = buildScheduleTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSCMMaterial() {
        return !DEPENDENCY_MATERIAL_TYPE.equalsIgnoreCase(this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialRevision that = (MaterialRevision) o;
        return Objects.equal(fingerprint, that.fingerprint) &&
                Objects.equal(revision, that.revision) &&
                Objects.equal(type, that.type) &&
                Objects.equal(buildScheduleTime, that.buildScheduleTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fingerprint, revision, type, buildScheduleTime);
    }
}
