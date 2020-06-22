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

function noop() {}

function el(parent, selector) {
  return (selector ? parent.querySelector(selector) : parent);
}

function els(parent, selector) {
  return (selector ? [].slice.call(parent.querySelectorAll(selector)) : [parent]);
}

function text(parent, selector) {
  return el(parent, selector).textContent.trim();
}

/** @private helper to click on elements; works with SVG elements, where el.click() does not. */
function clickOn(parent, selector) {
  const e = document.createEvent("MouseEvents");
  e.initMouseEvent("click", true, true, window);
  el(parent, selector).dispatchEvent(e);
}

function chartStub(overrides = {}) {
  const outer = document.createElement("div"), inner = document.createElement("div");
  outer.append(inner);
  return Object.assign({
    renderTo: outer, container: inner, series: [], options: {},
    hideLoading: noop, showLoading: noop,
    redraw: noop, zoomOut: noop,
    hasData: noop, hideNoData: noop, showNoData: noop
  }, overrides);
}

function MockFactories(assertionMap) {
  function noop() {}

  this.get = function findFactory(id) {
    return {
      config: (data, transport) => { // eslint-disable-line no-unused-vars
        return {
          title: { text: id },
          plotOptions: { series: { events: { afterAnimate: assertionMap[id] } } },
          series: [
            {name: `${id} - series 1`, data: data}
          ]
        };
      },
      params: noop
    };
  };
}

function MockTransport(response) {
  /** various callback hooks for testing */
  let inspectRequest, afterDone, afterFail, afterAlways;

  this.inspectRequest = function(fn) {
    inspectRequest = fn;
  };

  this.afterDone = function(fn) {
    afterDone = fn;
  };

  this.afterFail = function(fn) {
    afterFail = fn;
  };

  this.afterAlways = function(fn) {
    afterAlways = fn;
  };

  function exec() {
    const args = [].slice.call(arguments),
            fn = args.shift();
    if ("function" === typeof fn) {
      fn.apply(this, args);
    }
    return null;
  }

  this.request = function(key, params) {
    inspectRequest = exec(inspectRequest, key, params);

    const res = (response instanceof Array) ? response.shift() : response;

    return {
      done: function(fn) {
        if (!res.error) {
          setTimeout(function fireSuccess() { fn(JSON.stringify(res.data)); afterDone = exec(afterDone, res.data); }, 0);
        }
        return this;
      },
      fail: function(fn) {
        if (res.error) {
          setTimeout(function fireFailure() { fn(res.error); afterFail = exec(afterFail, res.error); }, 0);
        }
        return this;
      },
      always: function(fn) {
        setTimeout(function fireAlways() { fn(); afterAlways = exec(afterAlways); }, 0);
        return this;
      }
    };
  };
}

export {el, els, text, clickOn, noop, chartStub, MockFactories, MockTransport};
