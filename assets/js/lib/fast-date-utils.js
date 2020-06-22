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

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
        MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
       ONE_SEC = 1000,
       ONE_MIN = 60 * ONE_SEC,
      ONE_HOUR = 60 * ONE_MIN,
       ONE_DAY = 24 * ONE_HOUR,
     BASE_DATE = new Date(new Date(ONE_DAY).setHours(0, 0, 0, 0));

function axisFmt(date) {
  return WEEKDAYS[date.getDay()] + " " + ("00" + date.getDate()).slice(-2) + " " + MONTHS[date.getMonth()];
}

function relativeTime(date) {
  const offset = date - startOfDay(date, true);
  return new Date(BASE_DATE.getTime() + offset);
}

function endOfDay(date, asCopy) {
  if (asCopy) { date = new Date(date); }
  date.setHours(23, 59, 59, 999);
  return date;
}

function startOfDay(date, asCopy) {
  if (asCopy) { date = new Date(date); }
  date.setHours(0, 0, 0, 0);
  return date;
}

function addDays(date, numDays, asCopy) {
  if (asCopy) { date = new Date(date); }
  date.setTime(date.getTime() + (numDays * ONE_DAY));
  return date;
}

function addHours(date, numHours, asCopy) {
  if (asCopy) { date = new Date(date); }
  date.setTime(date.getTime() + (numHours * ONE_HOUR));
  return date;
}

// comparison operators; fill these out as needed

function eq(a, b) {
  return a.getTime() === b.getTime();
}

function lte(a, b) {
  return a.getTime() <= b.getTime();
}

export default { axisFmt, addDays, addHours, startOfDay, endOfDay, relativeTime, eq, lte };
