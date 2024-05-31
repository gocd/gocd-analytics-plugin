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

import "css/global";
import "css/agent-state-transition-chart";

import AnalyticsEndpoint from "gocd-server-comms";
import Agents from "../../charts/agents";

import $ from "jquery";
import moment from "../../lib/moment-humanize-for-gocd";
import SimpleStackedBar from "../../santosh/simple-stacked-bar";
import drawChartStats from "./header";
import showLegend from "./footer";

let transportRef, agentUUID;
const DATE_FMT = "YYYY-MM-DD";
const TODAY = moment().format(DATE_FMT);

const range = [
    {id: "30", text: "Last 30 Days", start: moment(TODAY).subtract(30, "days").format(DATE_FMT), selected: true},
    {id: "7", text: "Last 7 Days", start: moment(TODAY).subtract(7, "days").format(DATE_FMT), selected: false},
    {id: "1", text: "Last 24 Hours", start: moment(TODAY).subtract(1, "days").format(DATE_FMT), selected: false}
];

// For whatever reason, using _.find() seems to break unrelated charts...???
// e.g. using _find(range, (r) => r.id === id) causes the pipeline labels
// to disappear from bar charts. Really don't know, and spent too much time
// looking into it.
function byId(id) {
    for (let i = 0, len = range.length; i < len; ++i) {
        if (range[i].id === id) return range[i];
    }
    return null;
}

function renderGanttChart(initialData, transport) {
    const parsedData = JSON.parse(initialData);

    console.log('parsedData = ', parsedData);

    transportRef = transport;
    agentUUID = parsedData.uuid;
    const transitions = parsedData.transitions;
    const config = Agents.transitions("chart-container", transitions, range, redraw);

    console.log('config = ', config);

    // Gantt.draw(config);

    // document.getElementById("chart-container").style.height = "350px";

    drawChartStats(range, fetchAndRedraw);
    new SimpleStackedBar("Agent State Transitions", parsedData);
    showLegend();
}

function setSelection(days) {
    range.forEach((r) => {
        r.selected = (days === r.id);
    });
}

function showSpinner() {
    $(".spinner-overlay").removeClass("hidden");
}

function hideSpinner() {
    $(".spinner-overlay").addClass("hidden");
}

function redraw(days) {
    days = days.toString();
    setSelection(days);

    const params = {
        agent_uuid: agentUUID,
        start: byId(days).start,
        end: TODAY,
        type: "agent",
        metric: "agent_state_transition"
    };

    showSpinner();

    console.log('in redraw() about to request from fetch-analytics, params = ', params);

    transportRef.request("fetch-analytics", params).done((data) => {
        renderGanttChart(data, transportRef);
        hideSpinner();
    }).fail(console.error); //eslint-disable-line no-console
}

function fetchAndRedraw(days) {
    const params = {
        agent_uuid: agentUUID,
        start: byId(days).start,
        end: TODAY,
        type: "agent",
        metric: "agent_state_transition"
    };

    console.log('fetching latest data for', params);

    transportRef.request("fetch-analytics", params).done((data) => {
        renderGanttChart(data, transportRef);
    }).fail(console.error);

}

AnalyticsEndpoint.onInit(function (initialData, transport) {
    renderGanttChart(initialData, transport);
});

AnalyticsEndpoint.ensure("v1");
