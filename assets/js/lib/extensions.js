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

import DevTools from "./devtools.js";

/**
 * Define custom modifications here. Not for loading vendored modules -- please
 * use js/lib/load-highcharts.js for that.
 */
function Extensions() {
  "use strict";

  let intialized = false;

  function addEventForSetData(H) {
    H.wrap(H.Series.prototype, "setData", function(proceed, data, redraw, animation, updatePoints) {
      let allow = false;

      H.fireEvent(this, "data", {data}, function allowSetData() { allow = true; });

      if (allow) {
        return proceed.apply(this, [data, redraw, animation, updatePoints]);
      }
    });
  }

  function enableArrowsOnSharedTooltips(H) {
    H.wrap(H.Tooltip.prototype, "move", function(proceed, x, y, anchorX, anchorY) {
      // the following will force `skipAnchor` to be false
      this.followPointer = false;
      this.len = 1;

      // continue with original Tooltip.prototype.move()
      return proceed.apply(this, [x, y, anchorX, anchorY]);
    });

    H.wrap(H.Tooltip.prototype, "getAnchor", function(proceed, points, mouseEvent) {
      if (this.shared && "area" === this.chart.options.chart.type) {
        points = [H.splat(points)[0]];
      }

      return proceed.apply(this, [points, mouseEvent]);
    });
  }

  function disableTooltipsWhenDragging(H) {
    H.wrap(H.Pointer.prototype, "drag", function(proceed, e) {
      this.chart.tooltip.update({enabled: false});

      return proceed.apply(this, [e]);
    });

    H.wrap(H.Pointer.prototype, "drop", function(proceed, e) {
      this.chart.tooltip.update({enabled: true});

      return proceed.apply(this, [e]);
    });
  }

  function allowTooltipVisibilityControlWithClass(H) {
    H.wrap(H.Tooltip.prototype, "hide", function(proceed, delay) {
      if ("string" === typeof this.options.containerClassOnHide) {
        if (this.container) {
          this.container.classList.add(this.options.containerClassOnHide);
        }
      }

      proceed.call(this, delay);
    });

    H.wrap(H.Tooltip.prototype, "move", function(proceed, x, y, anchorX, anchorY) {
      if (this.isHidden && "string" === typeof this.options.containerClassOnHide) {
        if (this.container) {
          this.container.classList.remove(this.options.containerClassOnHide);
        }
      }

      proceed.call(this, x, y, anchorX, anchorY);
    });
  }

  /**
   * @private Throttles a function with window.requestAnimationFrame()
   *
   * Useful for event handlers that may fire very rapidly (e.g. window.onresize)
   */
  function throttleRaf(fn) {
    let isRunning, self, args;

    function run() {
      isRunning = false;
      fn.apply(self, args);
    }

    return function throttledFn() {
      self = this;
      args = Array.prototype.slice.call(arguments);

      if (isRunning) {
        return;
      }

      isRunning = true;
      window.requestAnimationFrame(run);
    };
  }

  function responsiveResize(H) {
    H.wrap(H, "chart", function(proceed, el, config, callback) {
      config.chart = config.chart || {};

      if (config.chart.noResize) {
        let chart = proceed.apply(H, [el, config, callback]);

        return chart;
      }

      config.chart.width = window.window.innerWidth;
      config.chart.height = window.window.innerHeight;

      let chart = proceed.apply(H, [el, config, callback]);

      if ("function" === typeof config.resizer) config.resizer.apply(chart);

      window.onresize = throttleRaf(function resizeChart() {
        chart.setSize(window.window.innerWidth, window.innerHeight, false);
        if ("function" === typeof config.resizer) config.resizer.apply(chart);
      });

      return chart;
    });
  }

  function enableDevTools(H) {
    H.wrap(H, "chart", function(proceed, el, config, callback) {
      const chart = proceed.apply(H, [el, config, callback]);
      DevTools.variantsToggler(config.variants).apply(chart);
      window.onkeydown = DevTools.keySequenceListener(["k", "i"], DevTools.chartInspector(chart));

      return chart;
    });
  }

  this.setup = function setup(H) {
    if (!intialized) {
      addEventForSetData(H);
      enableArrowsOnSharedTooltips(H);
      disableTooltipsWhenDragging(H);
      allowTooltipVisibilityControlWithClass(H);
      responsiveResize(H);
      enableDevTools(H);
      intialized = true;
    }
    return H;
  };
}

export default new Extensions();
