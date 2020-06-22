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

import * as d3 from "d3";

const drawGanttChart = function (selector, config) {
  const margin = {
    top: 0,
    right: 25,
    bottom: 18, // this should be the height of the horizontal axis
    left: 65
  };

  const HEIGHT = 310;
  const WIDTH = document.body.clientWidth - margin.left + 20; //consider padding added by css

  const TICK_FORMAT = "%H:%M";

  const LOWER_VERTICAL_BOUND = HEIGHT - margin.top - margin.bottom;

  const yAxisData = config.data.map((d) => d.date);
  const allStatus = config.status.reduce((hash, state) => {
    hash[state] = state.toLowerCase();
    return hash;
  }, {});

  let timeDomainStart, timeDomainEnd;
  let xScale, yScale, xAxis, yAxis;

  const keyFunction = function (d) {
    return d.startDate + d.status + d.endDate;
  };

  const rectTransform = function (d) {
    return "translate(" + xScale(d.startDate) + "," + yScale(d.date) + ")";
  };

  const initTimeDomain = function (data) {
    if (data === undefined || data.length < 1) {
      timeDomainStart = d3.timeDay.offset(new Date(), -3);
      timeDomainEnd = d3.timeHour.offset(new Date(), +3);
      return;
    }
    data.sort(function (a, b) {
      return a.endDate - b.endDate;
    });

    timeDomainEnd = data[data.length - 1].endDate;
    data.sort(function (a, b) {
      return a.startDate - b.startDate;
    });
    timeDomainStart = data[0].startDate;
  };

  const initAxis = function () {
    xScale = d3.scaleTime().domain([timeDomainStart, timeDomainEnd]).range([0, WIDTH - margin.left]).clamp(true);
    yScale = d3.scaleBand().domain(yAxisData).range([0, LOWER_VERTICAL_BOUND]).padding(.1);
    xAxis = d3.axisBottom(xScale).tickFormat(d3.timeFormat(TICK_FORMAT));
    yAxis = d3.axisLeft(yScale).tickSize(0);
  };

  //start drawing Chart
  initTimeDomain(config.data);
  initAxis();

  const svg = d3.select(selector)
    .append("svg").attr("class", "chart").attr("width", WIDTH).attr("height", HEIGHT)
    .append("g")
    .attr("class", "gantt-chart")
    .attr("transform", "translate(" + (margin.left) + ", " + (margin.top) + ")");

  const tooltip = d3.select("body").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);

  svg.selectAll(".chart")
    .data(config.data, keyFunction).enter()
    .append("rect")
    .attr("class", function (d) {
      return allStatus[d.status];
    })
    .attr("y", 0)
    .attr("transform", rectTransform)
    .attr("height", function () {
      return yScale.bandwidth();
    })
    .attr("width", function (d) {
      return Math.max(1, (xScale(d.endDate) - xScale(d.startDate)));
    })
    .on("mouseover", function (d) {
      tooltip.transition()
        .style("opacity", .9);
      tooltip.html(config.asTooltip(d))
        .style("left", (d3.event.pageX - 115) + "px")
        .style("top", (d3.event.pageY - 65) + "px");
    })
    .on("mouseout", function () {
      tooltip.transition().style("opacity", 0);
    });

  svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0, " + LOWER_VERTICAL_BOUND + ")")
    .transition()
    .call(xAxis);

  svg.append("g").attr("class", "y axis").transition().call(yAxis);
};

const generateLegendFor = function (allStatus) {
  return allStatus.map((status) => {
    return `<div class="legend-item">
              <span class="legend-dot ${status.toLowerCase()}"></span>
              <span class="legend-text">${status}</span>
            </div>`;
  }).join("\n");
};

export default {
  draw: function (config) {
    const container = document.getElementById(config.selector);
    const content = `<div>
      <div class="spinner-overlay hidden">
        <div class="spinner">Loading...</div>
      </div>
      <div class="title-container">
        <div class="title"> ${config.title} </div>
        ${config.rangeSelector}
      </div>
      <div class="subtitle-container">
        ${config.subtitle}
      </div>
      <div id="gantt-chart-container"></div>
      <div class="legend">${generateLegendFor(config.status)}</div>
    </div>`;

    container.innerHTML = content;

    if (config.data.length) {
      drawGanttChart("#gantt-chart-container", config);
    } else {
      const message = document.createElement("span");
      message.classList.add("no-data-message");
      message.textContent = "No data to display for selected time period";

      const noData = document.createElement("div");
      noData.classList.add("no-data");
      noData.append(message);

      document.getElementById("gantt-chart-container").append(noData);
    }
  }
};
