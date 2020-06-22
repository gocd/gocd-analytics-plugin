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

import moment from "./moment-humanize-for-gocd.js";
import $ from "jquery";
import H from "js/lib/load-highcharts.js";

const DATE_FMT = "YYYY-MM-DD";

function RangeSelector(transport, chart, options) {
  const TODAY      = moment().format(DATE_FMT),
        params     = options.params,
        callback   = options.callback,
        transforms = options.transforms;

  function updateData(chart, data) {
    let redraw = false;

    $.each(chart.series, (i, s) => {
      if ("function" === typeof transforms[s.name]) {
        s.setData(transforms[s.name](data));
        redraw = true;
      }
    });

    if (redraw) {
      chart.redraw();
      chart.zoomOut();
      if (callback) {
        callback(transport);
      }
    }

    if (!chart.hasData()) {
      chart.showNoData();
    }

    H.fireEvent(chart, "range");
  }

  function changeRange(e) {
    e.stopPropagation();
    e.preventDefault();
    const current = $(e.currentTarget);

    $(chart.ranger).find(".selected").removeClass("selected");
    current.addClass("selected");

    chart.hideNoData();
    chart.showLoading();

    transport.request("fetch-analytics", $.extend({}, params, current.data("range-params"))).
      done((data) => {
        chart.hideLoading(); // NOTE: need to do call this before updateData() or else "no data" message won't show;
        updateData(chart, JSON.parse(data));
      }).
      fail(console.error). // eslint-disable-line no-console
      always(() => chart.hideLoading()); // in case we fail early
  }

  chart.ranger = $("<div class=\"range-selector\">").append($.map(options.buttons, (b) => {
    return $("<button class=\"range-button\">").
      text(b.text).
      attr("data-range-params", JSON.stringify({ start: b.start, end: TODAY })).
      toggleClass("selected", b.selected);
  })).on("click", ".range-button", changeRange)[0];

  $(chart.container).prepend(chart.ranger);
}

function RangeSelectorSupport() {
  this.inject = function inject(transport, chart, options) {
    options.buttons = this.defaultButtons;

    if (chart.ranger) {
      chart.container.removeChild(chart.ranger);
      delete chart.ranger;
    }

    H.merge(true, chart.options, { lang: { noData: "No data to display for selected time period" } });

    new RangeSelector(transport, chart, options);
  };

  this.defaultButtons = [
    { text: "Last 24 Hours", start: moment().subtract(1, "days").format(DATE_FMT), selected: false },
    { text: "Last 7 Days", start: moment().subtract(7, "days").format(DATE_FMT), selected: false },
    { text: "Last 30 Days", start: moment().subtract(30, "days").format(DATE_FMT), selected: true },
    { text: "All", start: "*", selected: false }
  ];
}

export default new RangeSelectorSupport();
