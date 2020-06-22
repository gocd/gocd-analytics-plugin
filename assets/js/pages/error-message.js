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
import "css/error";

import $ from "jquery";
import Utils from "../lib/utils.js";

import library from "js/lib/load-fontawesome";
import { faExclamationCircle } from "@fortawesome/free-solid-svg-icons";

library.add(faExclamationCircle);

$(document).ready(function () {
  const msg = window.location.search.match(/[&?]msg=([^&]+)/);
  const msgText = msg ? decodeURIComponent(msg[1]) : "An error occurred while generating analytics, please check the plugin logs.";

  $(document.body).html(Utils.errorMessage(msgText));
});
