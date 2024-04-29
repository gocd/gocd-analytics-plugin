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
// import H from "./load-highcharts.js";

/**
 * This "class" is responsible for managing a linear history of chart
 * configuration snapshots. It is intended to be hooked into drilldown
 * and drillup events. Currently, this is a private class, abstracted
 * the `DrilldownSupport.withDrilldown()`` helper.
 */
function DrilldownConfigCache() {
  const cache = []; // each element represents a drilldown level;

  /** creates a drilldown level-sandboxed place to snapshot option updates */
  this.backup = function backup(config) {
    cache.push(config);
  };

  /** returns the most recent drilldown config snapshot */
  this.restore = function restore() {
    const config = cache.pop();
    config.chart = config.chart || {};
    // H.merge(true, config.chart, frameDimensions());
    return config;
  };

  /** The current drilldown cache level */
  this.level = function level() {
    return cache.length;
  };

  /** debugging method to show the current cache contents */
  this.print = function print() {
    console.log("cached configs", cache); // eslint-disable-line no-console
  };
}

function frameDimensions() {
  return { width: window.innerWidth, height: window.innerHeight };
}

/** @private Sets the drilldown property to the next metric for all points in all series */
function markSeriesAsDrilldown(config, nextKey, drilldownHandler) {
  if (nextKey) {
    for (let i = 0, len = config.series.length, s; i < len; ++i) {
      s = config.series[i];
      const existingHandler = _.get(s, "point.events.click");

      s.className = s.className ? `${s.className} drilldown-series` : "drilldown-series";
      s.downstream = nextKey;

      _.set(s, "point.events.click", "function" === typeof existingHandler ? function(e) {
        return (existingHandler.apply(this, [e]) !== false) && (drilldownHandler.apply(this, [e]) !== false);
      } : drilldownHandler);
    }
  }

  return config;
}

function ensureDrillupButton(chart, drillup) {
  function drillupOnClick(e) {
    e.stopPropagation();
    e.preventDefault();

    drillup(chart);

    return false;
  }

  if (!chart.upstreamButton) {
    chart.upstreamButton = chart.renderer.button(
      "\u3031Back",
      null,
      null,
      drillupOnClick,
      { width: 59, height: 6, r: 3 }
    ).addClass("upstream-button").
    attr({ align: "right", zIndex: 7 }).
    add().
    align({ x: 0, y: 48, align: "right", verticalAlign: "top" }, false, "spacingBox");
  }
}

function snap(config) {
  return _.cloneDeep(config);
}

/** Public API to add async drilldown behavior to any chart. */
function DrilldownSupport() {
  this.withDrilldown = function withDrilldown(config, metricsPath, factories, transport) {

    const CONFIG = new DrilldownConfigCache();
    let $current;

    function nextMetric() {
      const next = CONFIG.level();
      return metricsPath[next];
    }

    function drillup(chart) {
      if (CONFIG.level() > 0) {
        const config = CONFIG.restore(),
             element = chart.renderTo;

        $current = snap(config);
        chart.destroy();

        // chart = H.chart(element, config);
        // if (CONFIG.level() > 0) ensureDrillupButton(chart, drillup);
      }
    }

    function drilldown(e) {
      e.stopPropagation();
      e.preventDefault();

      if (e.seriesOptions) return;

      let chart = this.series.chart;

      const element = chart.renderTo,
             metric = e.point.series.options.downstream,
            factory = factories.get(metric),
             params = factory.params(e.point);

      chart.showLoading();

      CONFIG.backup($current);

      transport.request("fetch-analytics", params).
        done((data) => {
          chart.hideLoading();
          chart.destroy();

          data = JSON.parse(data);

          const config = markSeriesAsDrilldown(
            factory.config(data, transport), nextMetric(), drilldown
          );

          $current = snap(config);

          // ensureDrillupButton(H.chart(element, config), drillup);
        }).
        fail(console.error); // eslint-disable-line no-console

      return false;
    }

    markSeriesAsDrilldown(config, nextMetric(), drilldown);

    $current = snap(config);

    return config;
  };
}

export default new DrilldownSupport();
