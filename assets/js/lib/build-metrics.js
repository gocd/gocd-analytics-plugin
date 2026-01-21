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

import _ from "lodash";
import moment from "js/lib/moment-humanize-for-gocd.js";
import Stats from "js/lib/stats.js";

function BuildMetrics() {
  "use strict";

  function mean(models) {
    return _.mean(models.map(model => {
      return (model.total_time_secs - model.time_waiting_secs) / 60.0;
    }));
  }

  function mttr(models) {
    const stats = models.reduce((memo, pipe) =>  {
      if (!memo.lastFailed && "failed" === pipe.result.toLowerCase()) {
        memo.lastFailed = pipe;
      } else if (memo.lastFailed && "passed" === pipe.result.toLowerCase()) {
        memo.ttr += new Date(pipe.last_transition_time) - new Date(memo.lastFailed.scheduled_at);
        memo.population += 1;
        memo.lastFailed = null;
      }

      return memo;
    }, {ttr: 0, population: 0, lastFailed: null});

    return 0 !== stats.population ? stats.ttr / stats.population : 0;
  }

  function runFrequency(models) {
    if (models.length <= 1) { return "N/A"; }
    const d = moment.duration(new Date(models[models.length - 1].scheduled_at) - new Date(models[0].scheduled_at), "ms").roundUp();
    return `${(models.length / d[0]).toFixed(2)} <span class="value-desc">per ${d[1]}</span>`;
  }

  function mtbf(models) {
    const stats = models.reduce((memo, pipe) =>  {
      const pipeStatus = pipe.result.toLowerCase();
      if (!memo.lastPassed && "passed" === pipeStatus) {
        memo.lastPassed = pipe;
      } else if (memo.lastPassed && "failed" === pipeStatus) {
        memo.ttr += new Date(pipe.last_transition_time) - new Date(memo.lastPassed.scheduled_at);
        memo.population += 1;
        memo.lastPassed = null;
      }

      return memo;
    }, {ttr: 0, population: 0, lastPassed: null});

    return 0 !== stats.population ? stats.ttr / stats.population : 0;
  }

  function failureRate(models) {
    const total = models.length;
    if (0 === total) { return 0; }
    const failures = models.filter(model => {
      return "failed" === model.result.toLowerCase();
    }).length;
    const x = Stats.gcd(failures, total);
    return `${failurePercent(models)} <span class="value-desc">(${failures / x} out of ${total / x} runs)</span>`;
  }

  function failurePercent(models) {
    if (0 === models.length) { return "0%"; }
    const failures = models.filter(model => {
      return "failed" === model.result.toLowerCase();
    }).length;
    return `${(failures / models.length * 100).toFixed(2)}%`;
  }

  function failureFrequency(models) {
    if (0 === models.length) { return 0; }
    const failures = models.filter(model => {
      return "failed" === model.result.toLowerCase();
    }).length;
    const d = moment.duration(new Date(models[models.length - 1].scheduled_at) - new Date(models[0].scheduled_at), "ms").roundUp();

    return `${(failures / d[0]).toFixed(2)} failures per ${d[1]}`;
  }

  this.mttr = mttr;
  this.mean = mean;
  this.failurePercent = failurePercent;
  this.failureFrequency = failureFrequency;
  this.failureRate = failureRate;
  this.mtbf = mtbf;
  this.runFrequency = runFrequency;
}

export default new BuildMetrics();
