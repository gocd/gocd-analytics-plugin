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

import {group} from "tape-plus";
import BuildMetrics from "js/lib/build-metrics.js";

group("MTTR", (test) => {
  test("should calculate correctly", (t) => {
    const expected = 300000,
      CREATED = new Date("2000-01-01T01:00:00");

    function ignoredFailedInstance() {
      // extra pipeline failures shouldn't matter
      // if they were preceded by failures
      return {
        result: "failed",
        scheduled_at: CREATED,
        last_transition_time: CREATED
      };
    }

    const models = [
      {
        result: "failed",
        scheduled_at: new Date("2018-01-01T16:00:00")
      },
      ignoredFailedInstance(),
      {
        result: "passed",
        last_transition_time: new Date("2018-01-01T16:05:00")
      },
      {
        result: "failed",
        scheduled_at: new Date("2018-01-01T19:00:00")
      },
      ignoredFailedInstance(),
      {
        result: "passed",
        last_transition_time: new Date("2018-01-01T19:05:00")
      },
      ignoredFailedInstance()
    ];

    const trimmed = models.filter((i) => {
      return CREATED.getTime() !== (i.scheduled_at && i.scheduled_at.getTime());
    });

    t.is(BuildMetrics.mttr(models), expected, "calc should measure times between a failure and the next success");
    t.is(BuildMetrics.mttr(trimmed), BuildMetrics.mttr(models), "calc should start measuring only on the first failure of a sequence until it encounters a success");
  });

  test("should return 0 if there are no failures", (t) => {
    const models = [ { result: "passed" }, { result: "passed" } ];
    t.is(BuildMetrics.mttr(models), 0, "cannot calculate when no failures present");
  });

  test("should return 0 if no models are provided", (t) => {
    t.is(BuildMetrics.mttr([]), 0, "cannot calculate when no data is present");
  });
});

group("MTBF", (test) => {
  test("should calculate correctly", (t) => {
    const expected = 10800000,
      CREATED = new Date("2000-01-01T01:00:00");

    function ignoredPassedInstance() {
      // extra pipeline failures shouldn't matter
      // if they were preceded by failures
      return {
        result: "passed",
        scheduled_at: CREATED,
        last_transition_time: CREATED
      };
    }

    const models = [
      {
        result: "passed",
        scheduled_at: new Date("2018-01-01T16:00:00")
      },
      ignoredPassedInstance(),
      {
        result: "failed",
        last_transition_time: new Date("2018-01-01T19:00:00")
      },
      {
        result: "failed",
        scheduled_at: new Date("2018-01-01T19:00:00")
      },
      {
        result: "passed",
        scheduled_at: new Date("2018-01-01T19:05:00")
      },
      ignoredPassedInstance(),
      {
        result: "passed",
        scheduled_at: new Date("2018-01-01T19:05:00")
      },
      ignoredPassedInstance()
    ];

    const trimmed = models.filter((i) => {
      return CREATED.getTime() !== (i.scheduled_at && i.scheduled_at.getTime());
    });

    t.is(BuildMetrics.mtbf(models), expected, "calc should measure times between a success and the next failure");
    t.is(BuildMetrics.mtbf(trimmed), BuildMetrics.mtbf(models), "calc should start measuring only on the first failure of a sequence until it encounters a success");
  });

  test("should return 0 if there are no failures", (t) => {
    const models = [ { result: "passed" }, { result: "passed" } ];
    t.is(BuildMetrics.mtbf(models), 0, "cannot calculate when no failures present");
  });

  test("should return 0 if no models are provided", (t) => {
    t.is(BuildMetrics.mtbf([]), 0, "cannot calculate when no data is present");
  });
});

group("run frequency", (test) => {
  test("return the expected text", (t) => {
    const models = [
      { scheduled_at: new Date("2018-01-01T19:00:00") },
      { scheduled_at: new Date("2018-01-08T19:00:00") }
    ];
    t.is(BuildMetrics.runFrequency(models), "2.00 <span class=\"value-desc\">per week</span>");
  });

  test("should return 0 if no models are provided", (t) => {
    t.is(BuildMetrics.runFrequency([]), "N/A", "should return N/A if none are provided");
  });
});

group("failure frequency", (test) => {
  test("return the expected text", (t) => {
    const models = [
      { scheduled_at: new Date("2018-01-01T19:00:00"), result: "failed" },
      { scheduled_at: new Date("2018-01-02T19:00:00"), result: "passed" },
      { scheduled_at: new Date("2018-01-03T19:00:00"), result: "passed" },
      { scheduled_at: new Date("2018-01-08T19:00:00"), result: "failed" },
      { scheduled_at: new Date("2018-01-08T19:00:00"), result: "passed" }
    ];
    t.is(BuildMetrics.failureFrequency(models), "2.00 failures per week");
  });

  test("should return 0 if no models are provided", (t) => {
    t.is(BuildMetrics.failureFrequency([]), 0, "should return 0 if none are provided");
  });
});

group("failure Rate", (test) => {
  test("return the expected text", (t) => {
    const models = [
      { result: "failed" },
      { result: "passed" },
      { result: "passed" },
      { result: "failed" },
      { result: "passed" },
      { result: "passed" }
    ];
    t.is(BuildMetrics.failureRate(models), "33.33% <span class=\"value-desc\">(1 out of 3 runs)</span>");
  });

  test("should return 0 if no models are provided", (t) => {
    t.is(BuildMetrics.failureRate([]), 0, "should return 0 if none are provided");
  });
});

group("failure %", (test) => {
  test("should calculate correctly", (t) => {
    const models = [
      { result: "passed" },
      { result: "failed" },
      { result: "failed" },
      { result: "failed" }
    ];
    t.is(BuildMetrics.failurePercent(models), "75.00%", "didn't calc the % correctly");
  });

  test("should return 0% if no models are provided", (t) => {
    t.is(BuildMetrics.failurePercent([]), "0%", "should return 0 if none are provided");
  });
});
