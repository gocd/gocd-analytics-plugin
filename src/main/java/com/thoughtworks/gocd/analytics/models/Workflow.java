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

import java.time.ZonedDateTime;

public class Workflow extends PersistentObject {
    private ZonedDateTime startTime;

    public Workflow() {
    }

    public Workflow(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workflow workflow = (Workflow) o;
        return Objects.equal(startTime, workflow.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startTime);
    }
}
