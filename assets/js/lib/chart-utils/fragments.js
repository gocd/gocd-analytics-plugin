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
import Stats from "../stats";

function Fragments() {
  this.shrug = function meh() {
    return "\u00AF\\_(\u30C4)_/\u00AF";
  };

  this.auxiliaryMetricsDropdown = function auxiliaryMetricsDropdown(metrics) {
    return  `
      <div class="auxiliary-metrics">
        ${metrics}
      </div>`;
  };

  this.calculateAxisMax = function calculateAxisMax(series) {
    const len = series[0].data.length,
          num = series.length,
         data = new Array(len);

    for (let i = 0, r = 0; i < len; data[i++] = r, r = 0) {
      for (let j = 0, y; j < num; ++j) {
        y = series[j].data[i].y;

        if ("number" === typeof y) {
          r += y;
        } else {
          console.error(`${y} (type: ${typeof y}) is not a number!`); // eslint-disable-line no-console
        }
      }
    }

    const maxVal = Math.max.apply(null, data),
           stats = Stats.stddev(Stats.filterOutliers(data, true).data),
       threshold = Math.round(stats.mean + (3 * stats.sd));

    return maxVal > threshold ? threshold : null;
  };

  function interlace(array, step) {
    const len = array.length, memo = [];
    // walk through array in reverse to ensure we include the
    // last tick, or else the graph may be truncated when the
    // last index is not an even multiple of `step`
    for (let i = len - 1, adj = len - 1 % step; i >= 0; i--) {
      if (0 === ((i - adj) % step)) memo.unshift(array[i]);
    }

    // Always include the zero position
    if (memo.length && memo[0] !== 0) memo.unshift(0);
    return memo;
  }

  function step(limit, width) {
    return Math.max(Math.round(limit / width), 2);
  }

  function calculateTicks(chart, ticks, thresh) {
    const horz = chart.plotWidth,
      numTicks = ticks.length, // tick positions
         tickW = numTicks > 0 ? Math.floor(horz / numTicks) : horz,
        padded = thresh + 24,
          skip = tickW < padded;

    return skip ? interlace(ticks, step(padded, tickW)) : ticks;
  }

  /**
   * Distributes tick positions so that labels can fit without being
   * rotated, wrapped, or truncated.
   *
   * @param avgWidth - The average label pixel width
   *
   * We explicitly pass in avgWidth because we can't calculate
   * label width during first render without a `redraw()`, as this
   * happens before `this.ticks` is populated. Calculating the
   * avgWidth happens to work in bar charts only because of the
   * resizer handler, as this forces a `redraw()` immediately after
   * initial render, but it it won't work as a general solution.
   * Thus, we can simply eyeball the value and pass it in as an
   * argument.
   */
  this.makeTickPositioner = function makeTickPositioner(avgWidth) {
    return function positionTicksToAccommodateLabels() {
      return calculateTicks(this.chart, this.tickPositions, avgWidth);
    };
  };

  this.makeTooltipPositioner = function makeTooltipPositioner() {
    return function positionTooltip(boxWidth, boxHeight, point) {
      const chart = this.chart,
            caret = 15, // height of arrow;
             xpad = 10;
      let x, y;

      if (this.shared && "area" === chart.hoverPoint.series.type) {
        // always choose the point in the series at the top of the stacking group
        point = chart.hoverPoints[0];
      }

      // prevent right edge of tooltip going beyond right edge of chart
      if (point.plotX + boxWidth + xpad > chart.chartWidth) {
        x = chart.chartWidth - boxWidth - xpad;
      } else {
        x = point.plotX;
      }

      // Note that higher y values are actually lower on the graph
      // prevent top of tooltip from moving above
      if (point.plotY - boxHeight < 0) {
        y = chart.plotTop + point.plotY + caret;
      } else {
        y = point.plotY + chart.plotTop - boxHeight - caret;
      }

      y = Math.max(chart.plotTop, Math.min(y, chart.plotTop + chart.plotHeight));

      return {
        x: x,
        y: y
      };
    };
  };

  this.chartTitle = function chartTitle(options) {
    const title = options.title,
       addendum = options.addendum,
          parts = [`<span class="title-text">${title}</span>`];

    if (addendum) parts.push(`<span class="auxiliary-text">${addendum}</span>`);

    return title ? {
      text: parts.join(" "),
      useHTML: true,
      align: "left",
      widthAdjust: 0
    } : { text: null };
  };

  this.chartSubtitle = function chartSubtitle(options) {
    return options.subtitle ? {
      text: options.subtitle,
      align: "left",
      x: 0,
      y: 64,
      widthAdjust: -80, // accommodate 75px + 5px margin for drillUp button
      useHTML: true
    } : { text: null };
  };

  this.chartLegend = function chartLegend(options) {
    return _.assign({
      align: "right",
      layout: "horizontal",
      verticalAlign: "bottom",
      padding: 8,
      margin: 12,
      itemDistance: 15,
      itemMarginTop: 2,
      itemMarginBottom: 2,
      reversed: true,
      x: 0,
      y: 0
    }, options.legend || {});
  };

  this.chartZoomHint = function chartZoomHint(options) {
    return _.assign({
      // ok, so this isn't really an axis title, but it's a super-
      // convenient place to add this. so sue me.
      text: [
        "Click and drag in the plot area to zoom in.",
        "Shift + drag to pan horizontally."
      ].join("<br/>"),
      reserveSpace: false,
      align: "low",
      y: 12
    }, options || {});
  };
}

export default new Fragments();
