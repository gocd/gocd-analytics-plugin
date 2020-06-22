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

function Stats() {
  "use strict";

  function avg(data) {
    for (var i = 0, sum = 0, len = data.length; i < len; sum += data[i++]);
    return sum / len;
  }

  function variance(data, mean /* optional, will be calculated if omitted */) {
    if ("number" !== typeof mean) mean = avg(data);
    for (var i = 0, len = data.length, diff, sq = []; i < len; diff = data[i++] - mean, sq.push(diff * diff));
    return avg(sq);
  }

  function stddev(data) {
    var mean = avg(data);
    return { mean: mean, sd: Math.sqrt(variance(data, mean)) };
  }

  function gcd(a, b) { // greatest common divisor
    if (b) {
      return gcd(b, a % b);
    } else {
      return Math.abs(a);
    }
  }

  function numericSort(a, b) { return a - b; }

  /**
   * Removes outliers from a data set.
   *
   * @param data - the original data
   * @param winsorize - [OPTIONAL] when set to true, outliers are replaced by
   *                    the min or max bounds; when set to false, outliers are
   *                    removed from the set
   *
   * High outliers are values >= 3rd quartile + 1.5 * the inter-quartile range (IQR)
   * Low outliers are values <= 1st quartile - 1.5 * IQR
   *
   * Always returns an object containing the outlier bounds (min, max) and a sorted
   * copy of the data, with outliers removed.
   */
  function filterOutliers(data, winsorize /* optional boolean */) {
    var values = data.slice().sort(numericSort),
      len = values.length, p25 = len / 4, p75 = len * 0.75,
      q1, q3, iqr, maxValue, minValue, result = [];

    if (data.length < 4) {
      return {min: Math.min.apply(0, data), max: Math.max.apply(0, data), data: values};
    }

    // find quartiles for IQR calculation
    if (0 === (len % 4)) {
      // take midpoint when evenly split
      q1 = (values[p25] + values[p25 + 1]) / 2;
      q3 = (values[p75] + values[p75 + 1]) / 2;
    } else {
      q1 = values[Math.floor(p25 + 1)];
      q3 = values[Math.ceil(p75 + 1)];
    }

    iqr = q3 - q1;
    maxValue = q3 + iqr * 1.5;
    minValue = q1 - iqr * 1.5;

    for (var i = 0, x; i < len; ++i) {
      x = values[i];
      if (winsorize) {
        result.push(Math.max(minValue, Math.min(maxValue, x)));
      } else {
        if ((x >= minValue) && (x <= maxValue)) result.push(x);
      }
    }
    return {min: minValue, max: maxValue, data: result};
  }

  this.avg = avg;
  this.variance = variance;
  this.stddev = stddev;
  this.filterOutliers = filterOutliers;
  this.gcd = gcd;
}

export default new Stats();
