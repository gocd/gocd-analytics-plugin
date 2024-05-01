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

    console.log('updating chart size with w, h = ', dynamicWidth, dynamicHeight);

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
    console.log('🧩 asyncRequest() transport, requestParams ', transport, requestParams);
    return new Promise((resolve) => {
        transport.request("fetch-analytics", requestParams)
            .done((data) => resolve(JSON.parse(data)))
            .fail(console.error.toString());
    });
}

async function addOptionsToSelect(selector, options) {

    options.forEach((option) => {
        const selectOption = document.createElement("option");
        selectOption.setAttribute("value", option);
        selectOption.text = option;

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


export {
    secondsToHms,
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
    getTimeFromTimestampString
}