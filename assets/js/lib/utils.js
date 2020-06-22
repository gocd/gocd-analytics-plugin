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
import moment from "./moment-humanize-for-gocd.js";
import Constants from "./constants.js";
import library from "js/lib/load-fontawesome";
import { faInfoCircle } from "@fortawesome/free-solid-svg-icons";

library.add(faInfoCircle);

function Utils() {
  this.addOnLoad = function addOnLoad(config, fn) {
    const existing = _.get(config, "chart.events.load");

    if ("function" === typeof existing) {
      _.set(config, "chart.events.load", function onload(e) {
        existing.apply(this, [e]);
        fn.apply(this, [e]);
      });
    } else {
      _.set(config, "chart.events.load", fn);
    }
  };

  this.infoMessage = function infoMessage(msgText) {
    return `<div class="info-message">
              <i class="fas fa-info-circle"></i>
              <p>${msgText}</p>
            </div>`;
  };

  this.errorMessage = function errorMessage(msgText) {
    return `<div class="error-message">
              <i class="fas fa-exclamation-circle"></i>
              <p>${msgText}</p>
            </div>`;
  };

  this.colorByResult = function colorByResult(result) {
    return Constants.COLORS[result.toLowerCase()];
  };

  /** tooltip formatter for key -> value data */
  this.tooltipKeyVal = function tooltipKeyVal(key, value, prefix) {
    prefix = prefix ? `${prefix}-` : "";
    const valEl = `<span class="${prefix}val">${value}</span>`;
    return key ? `<strong class="${prefix}key">${key}</strong>: ${valEl}` : valEl;
  };

  this.resultIndicator = function resultIndicator(result) {
    return `<span class="result-${result.toLowerCase()}">\u25CF</span>`;
  };

  this.instanceTooltipIndicator = function instanceTooltipIndicator(counter, result) {
    return this.tooltipKeyVal("Instance", `${counter} ${this.resultIndicator(result)}`);
  };

  /**
   * Generates tooltip markup that shows a colored indicator based on a result
   * string.
   *
   * We use the result string as the argument because that is common data
   * between series, as point.color may not be set for certain series data.
   * The color property is inaccessible in the other series if that series is
   * hidden via user interaction on the chart.
   */
  this.resultTooltipEntry = function resultTooltipEntry(result) {
    return this.tooltipKeyVal("Status", `${result} ${this.resultIndicator(result)}`);
  };

  /** Generates tooltip markup to display series duration data with minute-second resolution */
  this.appendDurationData = function appendDurationData(header, points, unit) {
    return _.reduce(points, (memo, pt) => {
      return `${memo}<br/>${this.tooltipKeyVal(pt.series.name, moment.duration(pt.y, unit).humanizeForGoCD())}`;
    }, header);
  };

  this.withTooltipFooter = function withTooltipFooter(message, content) {
    return `${content}<br/><footer class="point-tooltip-footer">${message}</footer>`;
  };
}

export default new Utils();
