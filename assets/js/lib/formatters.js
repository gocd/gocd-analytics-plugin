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
import Utils from "./utils";
import moment from "./moment-humanize-for-gocd";


function Formatters() {
  const AGENT_IDENT_SEP = " => ";
  const BC_SEP = "<span> \u226b </span>";
  const PARTS = ["Pipeline", "Stage", "Job"];
  const self = this;

  this.agentIdentifier = function agentIdentifier(agent) {
    return [agent.agent_host_name, agent.uuid].join(AGENT_IDENT_SEP);
  };

  this.agentHostnameFormatter = function agentHostnameFormatter(value) {
    return _.escape("string" === typeof value ? value.split(AGENT_IDENT_SEP).shift() : value);
  };

  this.agentUuidFormatter = function agentUuidFormatter(value) {
    return _.escape("string" === typeof value ? value.split(AGENT_IDENT_SEP).pop() : value);
  };

  this.agentBreadcrumbFormatter = function agentBreadcrumbFormatter(value) {
    return `<strong class="key">Agent</strong>: <span class="val" title="UUID: ${self.agentUuidFormatter(value)}">${self.agentHostnameFormatter(value)}</span>`;
  };

  this.agentTooltipHeaderFormatter = function agentTooltipHeaderFormatter(value) {
    return [
      Utils.tooltipKeyVal("Agent", self.agentHostnameFormatter(value)),
      Utils.tooltipKeyVal("UUID", self.agentUuidFormatter(value))
    ].join("<br/>");
  };

  this.breadcrumbFormatter = function breadcrumbFormatter(name, options) {
    options = options || {};
    if ("string" !== typeof name) return name;
    if ("number" !== typeof options.startAt) options.startAt = 0;

    const keys = options.keys || PARTS;
    const result = _.reduce(name.split("/"), (memo, el, i) => {
      if (i >= options.startAt) {
        const key = keys[i];
        memo.push(Utils.tooltipKeyVal(_.escape(key), _.escape(el), key ? key.toLowerCase() : null));
      }
      return memo;
    }, []);

    if (options.reverse) result.reverse();

    return result.join(options.sep || BC_SEP);
  };

  this.auxiliaryMetric = function auxiliaryMetric(title, value, cssClass, helpIcon) {
    if (helpIcon) {
     title = `${title} <i class="fas fa-question-circle contextual-help" title=""></i>`;
    }
    return `
      <div class="auxiliary-metric ${cssClass}">
        <div class="metric-title">${title}</div>
        <div class="metric-value">${value}</div>
      </div>
      `;
  };

  this.makeTimingTooltipFormatter = function makeTimingTooltipFormatter(headerFormatter, footerMessage) {
    return function tooltipFormatter() {
      const header = headerFormatter ? headerFormatter(this) : `<strong>${this.x}</strong>`;
      return Utils.withTooltipFooter(
        footerMessage || "Click to view more details",
        Utils.appendDurationData(header, this.points, "m")
      );
    };
  };

  /** label formatter to humanize a duration */
  this.durationAxisFormatter = function durationAxisFormatter() {
    return moment.duration(this.value, "m").humanizeForGoCD();
  };

  // Exported constants
  this.BC_SEP = BC_SEP;
}

export default new Formatters();
