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

export {secondsToHms, timestampToWords, getUniqueDatesFromArray, groupObjectsByDate, updateChartSize}