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

import test from "tape-plus";
import Utils from "js/lib/utils";

test("Utils.tooltipKeyVal() should return the formatted string correctly", (t) => {
  t.is(Utils.tooltipKeyVal("one", "two"), "<strong class=\"key\">one</strong>: <span class=\"val\">two</span>", "should output a key-value format");
  t.is(Utils.tooltipKeyVal("one", "two", "prefixthing"), "<strong class=\"prefixthing-key\">one</strong>: <span class=\"prefixthing-val\">two</span>", "should accept a class prefix for the key-value components");
});

test("Utils.resultIndicator() should return the correctly formatted string", (t) => {
  t.is(Utils.resultIndicator("PaSsEd"), "<span class=\"result-passed\">\u25CF</span>", "should render a dot with the correct result classname (w/ case insensitive matching)");
});
