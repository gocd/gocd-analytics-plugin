/*
 * Copyright 2024 ThoughtWorks, Inc.
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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.sql.Date;
import java.time.ZonedDateTime;

public class AgentUtilizationSummary {

    @Expose
    @SerializedName("utilization_date")
    private Date utilizationDate;

    @Expose
    @SerializedName("idle_duration_secs")
    private long idleDurationSecs;

    @Override
    public String toString() {
        return "AgentUtilizationSummary{" +
            "utilizationDate=" + utilizationDate +
            ", idleDurationSecs=" + idleDurationSecs +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AgentUtilizationSummary that = (AgentUtilizationSummary) o;

        if (idleDurationSecs != that.idleDurationSecs) {
            return false;
        }
        return utilizationDate.equals(that.utilizationDate);
    }

    @Override
    public int hashCode() {
        int result = utilizationDate.hashCode();
        result = 31 * result + (int) (idleDurationSecs ^ (idleDurationSecs >>> 32));
        return result;
    }

    public Date getUtilizationDate() {
        return utilizationDate;
    }

    public void setUtilizationDate(Date utilizationDate) {
        this.utilizationDate = utilizationDate;
    }

    public long getIdleDurationSecs() {
        return idleDurationSecs;
    }

    public void setIdleDurationSecs(long idleDurationSecs) {
        this.idleDurationSecs = idleDurationSecs;
    }

    public AgentUtilizationSummary(Date utilizationDate, long idleDurationSecs) {
        this.utilizationDate = utilizationDate;
        this.idleDurationSecs = idleDurationSecs;
    }
}
