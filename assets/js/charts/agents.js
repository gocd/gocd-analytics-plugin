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
import Constants from "js/lib/constants";
import D from "js/lib/fast-date-utils";
import Formatters from "js/lib/formatters";
import barChart from "js/lib/chart-utils/bar";
import moment from "js/lib/moment-humanize-for-gocd";

function AgentCharts() {
  Object.assign(this, {
    mostUtilized: function mostUtilized(agents) {
      const data = {
        title: "Agents with the Highest Utilization",
        addendum: "(Average over the last 7 days)",
        xAxis: {
          formatter: function () {
            return Formatters.agentHostnameFormatter(this.value);
          },
          categories: agents.map(Formatters.agentIdentifier)
        },
        tooltip: {
          formatter: Formatters.makeTimingTooltipFormatter(function (cursor) {
            return Formatters.agentTooltipHeaderFormatter(cursor.x);
          }, "Click on bar for more info")
        },
        series: [
          {
            name: "Idle Time",
            colorIndex: Constants.COLORS.waiting,
            data: agents.map(function (a) {
              return {
                y: a.idle_duration_secs / 60.0,
                agent_host_name: a.agent_host_name,
                agent_uuid: a.uuid
              };
            })
          },
          {
            name: "Build Time",
            colorIndex: Constants.COLORS.building,
            data: agents.map(function (a) {
              return {
                y: a.building_duration_secs / 60.0,
                agent_uuid: a.uuid,
                agent_host_name: a.agent_host_name
              };
            })
          }
        ]
      };
      return barChart(data);
    },

    transitions: function (selector, transitions, range, redraw) {
      const agentStates = ["Idle", "Building", "Cancelled", "Missing", "LostContact", "Unknown"];
      const agentStateWithTimingInfo = [];

      for (let i = 0, current, next, len = transitions.length; i < len; ++i) {
        if (i === len - 1) {
          console.warn("I dont know how to handle the last transition"); // eslint-disable-line
          // TODO: this is the last transition which should be considered too.
          // last duration is from the last transition to the current time.Pending: what happens when the agent is deleted?
          continue;
        }

        current = transitions[i], next = transitions[i + 1];
        const currentDate = new Date(current["transition_time"]),
                 nextDate = new Date(next["transition_time"]),
             currentState = current["agent_state"];

        const startDate = D.startOfDay(currentDate, true),
                endDate = D.startOfDay(nextDate, true);

        if (!D.eq(startDate, endDate)) {
          let from = currentDate;

          while (D.lte(startDate, endDate)) {
            const to = D.eq(startDate, endDate) ? nextDate : D.endOfDay(startDate, true);
            const state = getAgentStateForTime(new Date(from) /* use a copy to guard from updates to startDate */, to, currentState);
            agentStateWithTimingInfo.push(state);

            from = D.addDays(startDate, 1); // does an in-place update; from and startDate reference same object
          }
        } else {
          const state = getAgentStateForTime(currentDate, nextDate, currentState);
          agentStateWithTimingInfo.push(state);
        }
      }

      return {
        selector,
        "status": agentStates,
        "data": agentStateWithTimingInfo,
        "title": "Agent State Transitions",
        "subtitle": getAgentStateTransitionSubtitle(agentStates, agentStateWithTimingInfo),
        "rangeSelector": getRangeSelector(range, redraw),
        "asTooltip": (datum) => getTooltip(datum.tooltip)
      };
    }
  });
}

function getRangeSelector(range, redraw) {
  window.redraw = redraw;

  return range.map((config) => {
    return `<button ${config.selected ? "class=\"selected\" " : ""}onclick="redraw('${config.id}')">${config.text}</button>`;
  }).join("\n");
}

function getTooltip(tooltip) {
  const current = tooltip.from, last = tooltip.to, state = tooltip.state;

  return `
    <span>
      <strong>Start Time</strong>: <span>${moment(current).format("DD-MM-YYYY HH:mm:ss Z")}</span><br/>
      <strong>End Time</strong>: <span>${moment(last).format("DD-MM-YYYY HH:mm:ss Z")}</span><br/>
      <strong>${state} Time</strong>: <span>${moment.duration(last - current, "ms").humanizeForGoCD()}</span><br/>
    </span>
  `;
}

function getAgentStateForTime(from, to, state) {
  return {
    "tooltip": { from, to, state },
    "startDate": D.relativeTime(from),
    "endDate": D.relativeTime(to),
    "date": D.axisFmt(from),
    "status": state
  };
}

function getAgentStateTransitionSubtitle(status, data) {
  const timings = {"total": 0};
  status.forEach(s => timings[s] = 0);

  data.forEach((d) => {
    let timeSpentInAState = d.endDate - d.startDate;
    timings.total = timings.total + timeSpentInAState;
    timings[d.status] = timings[d.status] + timeSpentInAState;
  });

  if (!timings.total) { return; }

  return `<div class="state-metrics">
      ${status.map(s => `<dl class="state-metric-item"> <dt class="key">${_.escape(s)}</dt> <dd class="val">${((timings[s] * 100) / timings.total).toFixed(1)}%</dd> </dl>`).join("")}
    </div>`;
}


export default new AgentCharts();
