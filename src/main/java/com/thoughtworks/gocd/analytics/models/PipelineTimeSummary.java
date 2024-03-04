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
import com.thoughtworks.gocd.analytics.db.PersistentObject;

public class PipelineTimeSummary {

    @Expose
    @SerializedName("passcount")
    private int passCount;

    @Expose
    @SerializedName("failcount")
    private int failCount;

    @Expose
    @SerializedName("cancelcount")
    private int cancelCount;

//    @Expose
//    @SerializedName("sum")
//    private int sum;

//    public PipelineTimeSummary(int sum) {
//        this.sum = sum;
//    }
//
//    public int getSum() {
//        return sum;
//    }
//
//    public void setSum(int sum) {
//        this.sum = sum;
//    }

        @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PipelineTimeSummary that = (PipelineTimeSummary) o;

        if (passCount != that.passCount) {
            return false;
        }
        if (failCount != that.failCount) {
            return false;
        }
        return cancelCount == that.cancelCount;
    }

    @Override
    public int hashCode() {
        int result = passCount;
        result = 31 * result + failCount;
        result = 31 * result + cancelCount;
        return result;
    }

    @Override
    public String toString() {
        return "PipelineTimeSummary{" +
            "passCount=" + passCount +
            ", failCount=" + failCount +
            ", cancelCount=" + cancelCount +
            '}';
    }

    public PipelineTimeSummary() {

    }

    public PipelineTimeSummary(int passCount, int failCount, int cancelCount) {
        this.passCount = passCount;
        this.failCount = failCount;
        this.cancelCount = cancelCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getCancelCount() {
        return cancelCount;
    }

    public void setCancelCount(int cancelCount) {
        this.cancelCount = cancelCount;
    }
}
