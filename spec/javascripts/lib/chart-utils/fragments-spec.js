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
import Fragments from "js/lib/chart-utils/fragments";

const group = test.group;
group("Fragments.chartTitle()", (test) => {

  test("should return a title with the correct text", (t) => {
    const expected = "<span class=\"title-text\">title</span>",
    actual = Fragments.chartTitle({title: "title"});
    t.is(actual.text, expected, "should match provided title");
  });

  test("should correctly add the addendum", (t) => {
    const expected = "<span class=\"title-text\">title</span> <span class=\"auxiliary-text\">ax</span>",
    actual = Fragments.chartTitle({title: "title", addendum: "ax"});
    t.is(actual.text, expected, "should append addendum");
  });

  test("should return a title with null text if no title is given", (t) => {
    t.is(Fragments.chartTitle({}).text, null, "should have no title");
  });

});

group("Fragments.chartSubtitle()", (test) => {

  test("should return a subtitle with the correct text and y", (t) => {
    const actual = Fragments.chartSubtitle({subtitle: "subtitle"});
    t.is(actual.text, "subtitle", "should match provided subtitle");
    t.is(actual.y, 64, "should calculate correct vertical offset");
  });

  test("should return a subtitle with null text if no subtitle is given", (t) => {
    t.is(Fragments.chartSubtitle({}).text, null, "should have no subtitle");
  });

});
