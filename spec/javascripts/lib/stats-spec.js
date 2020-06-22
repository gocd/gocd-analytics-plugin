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
import Stats from "js/lib/stats";

test("Stats.avg(data) should calc the avg", (t) => {
  const expected = 5.8;
  const data = [ 5, 6, 10, 4, 4 ];

  t.is(Stats.avg(data), expected, "should calculate the mean from a set of numbers");
});

test("Stats.variance(data, mean) should calc the variance", (t) => {
  const expected = 11;
  const data = [ 1, 2, 4, 5, 7, 11 ];
  t.is(Stats.variance(data, 5), expected, "variance should accept a mean param as an optimization");
});

test("Stats.variance(data) should calc the variance even without the mean", (t) => {
  const expected = 11;
  const data = [ 1, 2, 4, 5, 7, 11 ];
  t.is(Stats.variance(data), expected, "variance should calculate the mean when it is not provided");
});

test("Stats.stddev(data) should calc the stddev", (t) => {
  const expected = {
    mean: 5,
    sd: 3.3166247903554
  };
  const data = [ 1, 2, 4, 5, 7, 11 ];

  t.deepEqual(Stats.stddev(data), expected, "should output the mean and standard deviation");
});

test("Stats.filterOutliers(data) should remove outliers", (t) => {
  const data = [ 1, 2, 4, 5, 7, 11, 32768, 10, 6, 8, 5, 6, 33000, 3, 4, 3, 5 ];
  t.deepEqual(Stats.filterOutliers(data).data, [ 1, 2, 3, 3, 4, 4, 5, 5, 5, 6, 6, 7, 8, 10, 11 ], "should remove outliers from data and sort");
});

test("Stats.filterOutliers(data, true) should winsorize data", (t) => {
  const data = [ 1, 2, 4, 5, 7, 11, 32768, 10, 6, 8, 5, 6, 33000, 3, 4, 3, 5 ];
  const actual = Stats.filterOutliers(data, true);
  t.deepEqual(actual.data.length, data.length, "winsorizing should not change the sample size");
  t.deepEqual(actual.data, [ 1, 2, 3, 3, 4, 4, 5, 5, 5, 6, 6, 7, 8, 10, 11, 21.5, 21.5 ], "winsorizing should replace outliers with the nearest limit");
});
