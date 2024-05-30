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

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.analytics.db.PersistentObject;
import java.time.ZonedDateTime;
import java.util.Objects;

public class UniversalSummary extends PersistentObject {
    @Expose
    @SerializedName("full_table_name")
    private String fullTableName;

    @Expose
    @SerializedName("total_size")
    private String totalSize;

    public UniversalSummary() {
    }

    public UniversalSummary(String fullTableName, String totalSize) {
        this.fullTableName = fullTableName;
        this.totalSize = totalSize;
    }

    @Override
    public String toString() {
        return "UniversalSummary{" +
            "fullTableName='" + fullTableName + '\'' +
            ", totalSize='" + totalSize + '\'' +
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

        UniversalSummary that = (UniversalSummary) o;
        return fullTableName.equals(that.fullTableName) && totalSize.equals(that.totalSize);
    }

    @Override
    public int hashCode() {
        int result = fullTableName.hashCode();
        result = 31 * result + totalSize.hashCode();
        return result;
    }

    public String getFullTableName() {
        return fullTableName;
    }

    public void setFullTableName(String fullTableName) {
        this.fullTableName = fullTableName;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }
}
