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

import Tooltip from "jquery-ui/ui/widgets/tooltip";
import library from "js/lib/load-fontawesome";
import { faInfoCircle } from "@fortawesome/free-solid-svg-icons";

library.add(faInfoCircle);

function TooltipUtil() {
  /** jQuery Tooltip helper -- not for Highcharts data point tooltips */
  const setTooltip = function setTooltip(target, content, options) {
    options = options || {};
    new Tooltip({
      classes: {
        "ui-tooltip-content": "tooltip-content",
        "ui-tooltip": "tooltip-container"
      },
      content: content,
      position: options.position,
      close: function (e, ui) {
        const el = ui.tooltip;
        el.hover(function over() {
            el.stop(true).fadeTo(400, 1);
          },
          function out() {
            el.fadeOut(400, function() {
              el.remove();
            });
          });
      }
    }, target);
  };

  function isExternalLink(el) {
    return "a" === el.tagName.toLowerCase() &&
      "string" === typeof el.getAttribute("href") &&
      "_blank" === el.getAttribute("target");
  }

  function handleExternalLink(tooltip, transport) {
    tooltip.addEventListener("click", function navigate(e) {
      const el = e.target;

      if (isExternalLink(el)) {
        e.stopPropagation();
        e.preventDefault();

        transport.request("link-external", {url: el.getAttribute("href")});
        return false;
      }
    });
  }

  this.addTooltip = function tooltip(selector, position, tooltipContent, transport, moreLink) {
    const element = document.querySelector(selector),
      content = document.createElement("span");

    content.textContent = tooltipContent;

    if (moreLink) {
      const link = document.createElement("a");
      content.appendChild(link);

      link.innerHTML = "More&hellip;";
      link.setAttribute("href", moreLink);
      link.setAttribute("target", "_blank");

      handleExternalLink(content, transport);
    }

    setTooltip(element, content, {position: position});
  };
}

export default new TooltipUtil();
