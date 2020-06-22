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

function on(el, type, fn) {
  el.addEventListener(type, fn, false);
  return el;
}

function el(tag, attrs, content, isHtml) {
  const node = document.createElement(tag.toLowerCase());
  if (attrs) {
    for (let key in attrs) {
      node.setAttribute(key, attrs[key]);
    }
  }
  if (content) {
    if ("string" === typeof content) {
      if (isHtml) {
        node.innerHTML = content;
        return node;
      }
      content = document.createTextNode(content);
    }

    if (content.forEach) {
      content.forEach(function(child) {
        node.appendChild(child);
      });
    } else {
      node.appendChild(content);
    }
  }
  return node;
}

function makeVariants(variants) {
  const body = document.body;

  function renderVariantToolbox(chart) {
    _.forEach(body.querySelectorAll(".ui-variants-controls"), (n) => { body.removeChild(n); });

    const enabled = /[?&]ui=test(?:&.+)?$/.test(window.location.search);
    if (!enabled || !variants || !variants.length) return;

    var controls = el("div", {class: "ui-variants-controls"}, [
      el("span", {class: "presets"}, variants.reduce(function(memo, v, i) {
        if (!v.name || !v.config) throw new Error(`invalid variant ${JSON.stringify(v, null, 2)}`);

        const id = `__r-variant-${i}`;
        memo.unshift(on(el("label", {class: "ui-variant", for: id}, v.name), "click", function use() {
          chart.update(v.config);
        }));
        memo.unshift(el("input", {type: "radio", id: id, name: "variant", value: v.name}));
        return memo;
      }, [])),
      on(el("span", {class: "toggle"}), "click", function toggle() {
        controls.classList.toggle("open");
      })
    ]);

    body.insertBefore(controls, body.firstChild);
  }

  return function variantsController() {
    const chart = this;
    renderVariantToolbox(chart);
  };
}

function DevTools() {
  "use strict";

  function variantsToggler(variants) {
    return makeVariants(variants);
  }

  function keySequenceListener(keys, action) {
    const match = [];
    let seq = [];

    if ("string" === typeof keys && !!keys.length) {
      keys = keys.toUpperCase();
      for (let i = 0, len = keys.length; i < len; ++i) {
        match.push(keys.charCodeAt(i));
      }
    } else if (keys instanceof Array) {
      for (let i = 0, len = keys.length; i < len; ++i) {
        if ("string" === typeof keys[i] && "" !== keys[i]) {
          match.push(keys[i].toUpperCase().charCodeAt(0));
        }

        if ("number" === typeof keys[i]) {
          match.push(keys[i]);
        }
      }
    } else {
      return;
    }

    if (!match.length) return;

    return function listenForKeySequence(e) {
      e = e || window.event;

      if (e.metaKey) {
        let charCode = ("number" === typeof e.which) ? e.which : e.keyCode;

        if (charCode === match[seq.length]) { // char sequence is matching
          e.stopPropagation();
          e.preventDefault();
          seq.push(charCode);

          if (seq.length === match.length) {
            seq = []; // reset, sequence fully entered
            action();
          }

          return;
        }
      }
      seq = []; // reset, sequence does not match
    };
  }

  function chartInspector(chart) {
    var body = document.body;

    var debugButton = on(el("button", 0, "Debug"), "click", function debug() {
      console.log(chart); // eslint-disable-line no-console
      debugger; // eslint-disable-line no-debugger
    });

    var INITIAL_CONTENT = "/* the chart is available as both the variable `chart`; press `command-enter` or click `Eval` to evaluate. */";
    var HINT = "Click `Eval` to evaluate (vars in scope: `chart`).";
    var evaluatorInput = on(el("textarea", {class: "evaluator", placeholder: HINT}, INITIAL_CONTENT), "keydown", keySequenceListener([13], evaluateExpression));

    function evaluateExpression() {
      try {
        console.log((function(chart) { return eval(evaluatorInput.value); }).apply(window, [chart])); // eslint-disable-line no-console,no-unused-vars
        flash(true);
      } catch(e) {
        flash(false);
        console.error(e); // eslint-disable-line no-console
      }
    }

    function resetExpression() { evaluatorInput.value = ""; }

    var status = el("span");
    var timeout;
    function hide() { status.remove(); timeout = null; }

    function flash(success) {
      if ("number" === typeof timeout) clearTimeout(timeout);

      status.textContent = success ? "\u2714" : "\u2715";
      status.setAttribute("class", success ? "success" : "failure");

      evalButton.insertBefore(status, evalButton.firstChild);
      timeout = setTimeout(hide, 500);
    }

    var evalButton = on(el("button", 0, "Eval"), "click", evaluateExpression);
    var resetButton = on(el("button", 0, "Reset"), "click", resetExpression);

    var inspector = el("div", {class: "dev-tools-inspector"}, [
      debugButton, evaluatorInput, evalButton, resetButton
    ]);

    return function toggleChartInspector() {
      if (body.contains(inspector)) {
        inspector.remove();
      } else {
        body.insertBefore(inspector, body.firstChild);
      }
    };
  }

  this.keySequenceListener = keySequenceListener;
  this.chartInspector = chartInspector;
  this.variantsToggler = variantsToggler;
}

export default new DevTools();
