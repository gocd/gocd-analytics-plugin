function secondsToHms(d) {
  d = Number(d);

  if (d < 60) {
    return d + 's';
  }

  const h = Math.floor(d / 3600);
  const m = Math.floor((d % 3600) / 60);
  // var s = Math.floor((d % 3600) % 60);

  const hDisplay = h > 0 ? h + (h === 1 ? "h, " : "h, ") : "";
  const mDisplay = m > 0 ? m + (m === 1 ? "m" : "m") : "";
  // var sDisplay = s > 0 ? s + (s == 1 ? ' s' : ' s') : '';
  return hDisplay + mDisplay;
}

function millisecondsToHoursMinutes(milliseconds) {
  const seconds = milliseconds / 1000;
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes
      % 60;

  let result = "";

  if (hours > 0) {
    result += `${hours}h `;
  }

  if (minutes > 0) {
    result += `${minutes}m `;
  }

  result += `${remainingSeconds}s`;

  return result;
}

function timestampToWords(ts) {
  const options = {weekday: 'short', day: 'numeric', month: 'short'};
  const formattedDate = new Date(ts);
  return new Intl.DateTimeFormat('en-US', options).format(formattedDate);
}

function getUniqueDatesFromArray(dates) {

  const justDates = dates.map(d => d.split('T')[0]);

  const dateSet = [...new Set(justDates)];

  return dateSet;
}

function groupObjectsByDate(data) {

  const dateGroupArray = {};

  data.forEach(obj => {
    const date = obj.transition_time.split('T')[0];

    if (dateGroupArray[date]) {
      dateGroupArray[date].push(obj);
    } else {
      dateGroupArray[date] = [obj];
    }
  });

  return dateGroupArray;
}

function updateChartSize(chart, w, h) {
  var dynamicWidth = window.innerWidth * 1; // Adjust the multiplier as needed
  var dynamicHeight = window.innerHeight * 0.8; // Adjust the multiplier as needed

  if (w !== undefined && h !== undefined) {
    dynamicWidth = window.innerWidth * w;
    dynamicHeight = window.innerHeight * h;
  }

  // console.log('updating chart size with w, h = ', dynamicWidth, dynamicHeight);

  chart.resize({
    width: dynamicWidth,
    height: dynamicHeight
  });
}

function groupBy(list, key) {
  return list.reduce((acc, cur) => {
    const keyValue = cur[key];
    acc[keyValue] = acc[keyValue] || [];
    acc[keyValue].push(cur);
    return acc;
  }, {});
}

function uniqBy(list, property) {
  const seenValues = new Set();
  return list.filter((item) => {
    const value = item[property];
    if (!seenValues.has(value)) {
      seenValues.add(value);
      return true;
    }
    return false;
  });
}

function uniq(arr) {
  return [...new Set(arr)];
}

const color = [
  "#32C5E9",
  "#67E0E3",
  "#9FE6B8",
  "#FFDB5C",
  "#ff9f7f",
  "#fb7293",
  "#E062AE",
  "#E690D1",
  "#e7bcf3",
  "#9d96f5",
  "#8378EA",
  "#96BFFF",
  "#ffe9e3",
  "#c4c1e0"
];

const asyncRequest = async (transport, requestParams) => {
  console.log('🧩 asyncRequest() transport, requestParams ', transport,
      requestParams);
  return new Promise((resolve) => {
    transport.request("fetch-analytics", requestParams)
    .done((data) => resolve(JSON.parse(data)))
    .fail(console.error.toString());
  });
}

async function addOptionsToSelect(selector, options) {

  options.forEach((option) => {
    const selectOption = document.createElement("option");
    selectOption.setAttribute("value", option.value ? option.value : option);
    selectOption.text = option.text ? option.text : option;

    selector.appendChild(selectOption);
  });

  selector.selectedIndex = 0;
}

function getDateFromTimestampString(timestamp) {
  const dt = new Date(timestamp);

  const year = dt.getFullYear();
  const month = String(dt.getMonth() + 1).padStart(2, '0');
  const day = String(dt.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function getTimeFromTimestampString(dateTimeString) {
  const dateObject = new Date(dateTimeString);

  if (isNaN(dateObject.getTime())) {
    throw new Error(`Invalid date time string: ${dateTimeString}`);
  }

  const hours = dateObject.getHours().toString().padStart(2, '0');
  const minutes = dateObject.getMinutes().toString().padStart(2, '0');
  const seconds = dateObject.getSeconds().toString().padStart(2, '0');

  return `${hours}:${minutes}:${seconds}`;
}

function truncateString(text, truncateOrder, maxLength = 10) {
  if (truncateOrder === 'None' || text.length <= maxLength) {
    return text;
  } else if (truncateOrder === 'Last') {
    return text.substring(0, maxLength - 3) + "...";
  } else if (truncateOrder === 'Middle') {
    let numFirstChars = 2;
    let numLastChars = 2;

    if (text.length <= (numFirstChars + numLastChars + 3)) {
      return text; // String is already short enough
    }
    const middleLength = text.length - numFirstChars - numLastChars;
    if (middleLength <= 3) {
      // Not enough space for ellipsis, shorten last characters
      numLastChars = Math.max(0, middleLength);
    }
    return `${text.slice(0, numFirstChars)}...${text.slice(-numLastChars)}`;
  }

}

function formatDatePicker(date) {
  const month = date.toLocaleString('default', {month: 'long'}); // Get full month name
  const day = date.getDate().toString().padStart(2, '0'); // Get day with leading zero

  return `${month} ${day}`;
}

function getPreviousMonthDateInDBFormat() {
  const today = new Date();
  const date = new Date(today.getFullYear(), today.getMonth(), 1);
  console.log("date = ", date);
  const formattedDate = date.toISOString().split('T')[0];
  console.log("formattedDate = ", formattedDate);
  return formattedDate;
}

function getFirstDayOfTheCurrentMonth() {
  const today = new Date();
  today.setDate(1);
  return today.toISOString().slice(0, 10);
}

function getTodaysDateInDBFormat() {
  const today = new Date();
  const formattedDate = today.toISOString().split('T')[0];
  return formattedDate;
}

function getLatestAndOldestDates(dates) {
  if (!dates || dates.length === 0) {
    return {latest: null, oldest: null};
  }

  let latest = dates[0];
  let oldest = dates[0];

  for (let i = 1; i < dates.length; i++) {
    const date = dates[i];
    if (date > latest) {
      latest = date;
    } else if (date < oldest) {
      oldest = date;
    }
  }

  return {latest, oldest};
}

function convertDateObjectToDBDateFormat(date) {
  console.log("convertDateObjectToDBDateFormat()", date);
  const dateObj = new Date(date.dateInstance);
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // Pad month for single digits
  const day = String(dateObj.getDate()).padStart(2, '0'); // Pad day for single digits

  const formattedDate = `${year}-${month}-${day}`;
  return formattedDate;
}

function getHumanReadableDateFromDBFormatDate(dateString) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
    return "Invalid date format. Please use YYYY-MM-DD.";
  }

  const dateParts = dateString.split('-');
  const year = parseInt(dateParts[0], 10);
  const month = parseInt(dateParts[1], 10) - 1; // Months are 0-indexed in JavaScript
  const day = parseInt(dateParts[2], 10);

  // Create a Date object and format it with the desired locale
  const date = new Date(year, month, day);
  const options = {month: 'long', day: 'numeric'};
  return date.toLocaleDateString('en-US', options); // Adjust 'en-US' for your locale if needed

}

function getChartContainer() {
  return document.getElementById("chart-container");
}

function showGraphLoading() {
  // const chart_container = getChartContainer();
  // chart_container.innerHTML = "";
  // chart_container.classList.add("loader");
}

function hideGraphLoading() {
  // const chart_container = document.getElementById("chart-container");
  //   chart_container.classList.remove("loader");
  //   console.log("chart-container not found");
}

export {
  secondsToHms,
  millisecondsToHoursMinutes,
  timestampToWords,
  getUniqueDatesFromArray,
  groupObjectsByDate,
  updateChartSize,
  groupBy,
  uniqBy,
  uniq,
  color,
  asyncRequest,
  addOptionsToSelect,
  getDateFromTimestampString,
  getTimeFromTimestampString,
  truncateString,
  formatDatePicker,
  getPreviousMonthDateInDBFormat,
  getTodaysDateInDBFormat,
  convertDateObjectToDBDateFormat,
  getHumanReadableDateFromDBFormatDate,
  getFirstDayOfTheCurrentMonth,
  getLatestAndOldestDates,
  // showGraphLoading,
  // hideGraphLoading
}