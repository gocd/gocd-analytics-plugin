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

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.gocd.analytics.models.MaterialRevision;

import java.time.ZonedDateTime;

public class MaterialRevisionMother {
    public static MaterialRevision materialRevisionFrom(long id, String fingerprint, String revision, String type, ZonedDateTime buildScheduleTime) {
        MaterialRevision materialRevision = new MaterialRevision(fingerprint, revision, type, buildScheduleTime);
        materialRevision.setId(id);

        return materialRevision;
    }

    public static MaterialRevision materialRevisionWithId(long id) {
        MaterialRevision materialRevision = new MaterialRevision();
        materialRevision.setId(id);

        return materialRevision;
    }
}
