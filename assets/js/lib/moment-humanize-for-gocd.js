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

import moment from "moment";
import momentDurationFormatSetup from "moment-duration-format";

if ("undefined" === typeof moment.duration.fn.humanizeForGoCD) {
  momentDurationFormatSetup(moment);

  const MONTH_1 = moment.duration(1, "month").asMilliseconds();
  const WEEK_1 = moment.duration(1, "week").asMilliseconds();
  const HOURS_24 = moment.duration(24, "hour").asMilliseconds();
  const HOUR_1 = moment.duration(1, "hour").asMilliseconds();
  const MINUTE_1 = moment.duration(1, "minute").asMilliseconds();

  moment.duration.fn.humanizeForGoCD = function () {
    const d = moment.duration(this, "ms");

    if (d >= HOURS_24) {
      return d.format("d[d] h[h] m[m]");
    }

    if (d >= HOUR_1) {
      return d.format("h[h] m[m]");
    }

    if (d >= MINUTE_1) {
      return d.format("m[m]");
    }

    return d.format("s[s]");
  };

  moment.duration.fn.roundUp = function() {
    const d = moment.duration(this, "ms");

    if (d >= MONTH_1) {
      return [d.asMonths(), "month"];
    }

    if (d >= WEEK_1) {
      return [d.asWeeks(), "week"];
    }

    if (d >= HOURS_24) {
      return [d.asDays(), "day"];
    }

    if (d >= HOUR_1) {
      return [d.asHours(), "hour"];
    }

    if (d >= MINUTE_1) {
      return [d.asMinutes(), "minute"];
    }

    return [d.asSeconds() / 1000, "second"];
  };
}

export default moment;
