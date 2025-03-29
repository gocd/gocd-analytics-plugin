function drawChartStats(range, callback, data) {
    const myDiv = document.getElementById("chart-container-meta");
    myDiv.innerHTML = "";

    const titleContainer = document.createElement("div");
    // titleContainer.style = "width: 100%; height: 35px; border-bottom: 1px solid #ccc;";
    titleContainer.classList.add("title-container");

    const title = document.createElement("div");
    title.innerText = "Agent State Transitions";
    // title.style = 'font-family: "Open Sans","Helvetica Neue",Helvetica,Roboto,Arial,sans-serif; fill: #333; font-weight: 600; font-size: 16px;';
    title.classList.add("title");

    titleContainer.appendChild(title);

    // const timeSection = document.createElement("div");
    // timeSection.style = "text-align: right";
    //
    // titleContainer.appendChild(timeSection);

    // const last24Hour = document.createElement("a");
    // last24Hour.textContent = "Last 24 Hours";
    // last24Hour.href = 'javascript:void(0);';
    // last24Hour.style = "background-color:lightgrey; color:black;";
    // last24Hour.onclick = () => {
    //     console.log('last 24 hours clicked');
    //     this.callback();
    // };
    //
    // timeSection.appendChild(last24Hour);


    range.forEach(r => {
        const btn = document.createElement("button");
        btn.innerText = r.text;
        if (r.selected) {
            btn.classList.add("selected");
        }
        btn.onclick = () => {
            handleButtonClick(r.id, range);
            callback(r.id);
        }
        titleContainer.appendChild(btn);
    });

    myDiv.appendChild(titleContainer);
    myDiv.appendChild(subHeader(data));

}

function handleButtonClick(id, range) {
    range.forEach(r => {
        if (id === r.id) {
            r.selected = true;
        } else {
            r.selected = false;
        }
    })
}

function subHeaderStats(data, statuses) {

    const timings = {"total": 0};

    console.log("subHeaderStats data, statuses", data, statuses);

    statuses.forEach(s => timings[s] = 0);

    console.log("data transitions");

    data.forEach((d) => {
        console.log("d", d);

        let timeSpentInAState = d.endDate - d.startDate;

        console.log("timeSpentInAState", timeSpentInAState);

        timings.total = timings.total + timeSpentInAState;
        timings[d.status] = timings[d.status] + timeSpentInAState;
    });

    if(!timings.total) {
        console.log("subHeaderStats timings total is zero");
        return;
    }

    console.log("subHeaderStats timings", timings);

    return timings;
}

function subHeader(data) {
    const subTitleContainer = document.createElement("div");
    subTitleContainer.classList.add("subtitle-container");

    const stateMetrics = document.createElement("div");
    stateMetrics.classList.add("state-metrics");

    const statuses = ['Idle', 'Building', 'Cancelled', 'Missing', 'LostContact', 'Unknown'];

    const timings = subHeaderStats(data, statuses);

    statuses.forEach(status => {
        const dl = document.createElement("dl");
        dl.classList.add("state-metric-item");

        const dt = document.createElement("dt");
        dt.classList.add("key");
        dt.innerText = status.toString();

        const dd = document.createElement("dd");
        dd.classList.add("val");
        // dd.innerText = Math.floor(Math.random() * 11) + '%';
        dd.innerText = ((timings[status] * 100) / timings.total).toFixed(1) + '%';

        dl.appendChild(dt);
        dl.appendChild(dd);

        stateMetrics.appendChild(dl);
    });

    subTitleContainer.appendChild(stateMetrics);

    return subTitleContainer;
}

export default drawChartStats;