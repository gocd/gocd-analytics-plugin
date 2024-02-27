function drawChartStats(range, callback, am) {
    console.log('drawChartStats am = ', am);

    const myDiv = document.getElementById("chart-container-meta");
    myDiv.innerHTML = "";

    const highChartsMainClassContainer = document.createElement("div");
    highChartsMainClassContainer.classList.add("highcharts-container", "area-chart");
    highChartsMainClassContainer.style = "position: relative;overflow: hidden;width: 649px;height: 145px;text-align: left;line-height: normal;z-index: 0;-webkit-tap-highlight-color: rgba(0, 0, 0, 0);";

    const rangeSelectorDiv = document.createElement("div");
    rangeSelectorDiv.classList.add("range-selector");
    // title.style = 'font-family: "Open Sans","Helvetica Neue",Helvetica,Roboto,Arial,sans-serif; fill: #333; font-weight: 600; font-size: 16px;';

    // rangeSelectorDiv.classList.add("title");

    highChartsMainClassContainer.appendChild(rangeSelectorDiv);

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
        btn.classList.add("range-button");

        btn.innerText = r.text;
        if (r.selected) {
            btn.classList.add("selected");
        }
        btn.onclick = () => {
            handleButtonClick(r.id, range);
            callback(r.id);
        }
        rangeSelectorDiv.appendChild(btn);
    });

    const spanHighChartsTitle = document.createElement("span");
    spanHighChartsTitle.classList.add("highcharts-title");
    spanHighChartsTitle.style = "font-family: &quot;Lucida Grande&quot;, &quot;Lucida Sans Unicode&quot;, Arial, Helvetica, sans-serif; font-size: 18px; position: absolute; white-space: normal; margin-left: 0px; margin-top: 0px; left: 20px; top: 17px; color: rgb(51, 51, 51); width: 609px; display: block;";

    const spanTitleText = document.createElement("span");
    spanTitleText.classList.add("title-text");
    spanTitleText.innerText = "Pipeline Build Time";

    spanHighChartsTitle.appendChild(spanTitleText);


    highChartsMainClassContainer.appendChild(spanHighChartsTitle);
    highChartsMainClassContainer.appendChild(subHeader(am));

    myDiv.appendChild(highChartsMainClassContainer);

}

function handleButtonClick(id, range) {
    range.forEach(r => {
        if (id == r.id) {
            r.selected = true;
        } else {
            r.selected = false;
        }
    })
}

function subHeader(am) {

    console.log('subHeader am = ', am);

    const spanHighChartsSubTitle = document.createElement("span");
    spanHighChartsSubTitle.classList.add("highcharts-subtitle");
    spanHighChartsSubTitle.style = "font-family: &quot;Lucida Grande&quot;, &quot;Lucida Sans Unicode&quot;, Arial, Helvetica, sans-serif; font-size: 12px; position: absolute; white-space: normal; margin-left: 0px; margin-top: 0px; left: 20px; top: 72px; color: rgb(102, 102, 102); width: 529px; display: block;";

    const auxiliaryMetrics = document.createElement("div");
    auxiliaryMetrics.classList.add("auxiliary-metrics");

    // const metrics = ['Run Frequency', 'Mean Time to Recovery', 'Mean Time Between Failures', 'Failure Rate'];
    //
    // metrics.forEach(status => {
    //     const divAuxiliaryMetric = document.createElement("div");
    //     divAuxiliaryMetric.classList.add("auxiliary-metric");
    //
    //     const divMetricTitle = document.createElement("div");
    //     divMetricTitle.classList.add("metric-title");
    //     divMetricTitle.innerText = status.toString();
    //
    //     const divMetricValue = document.createElement("div");
    //     divMetricValue.classList.add("metric-value");
    //     divMetricValue.innerText = Math.floor(Math.random() * 11) + '%';
    //
    //     const spanValueDesc = document.createElement("span");
    //     spanValueDesc.classList.add("value-desc");
    //     spanValueDesc.innerText = 'per week';
    //
    //     divMetricValue.appendChild(spanValueDesc);
    //
    //     divAuxiliaryMetric.appendChild(divMetricTitle);
    //     divAuxiliaryMetric.appendChild(divMetricValue);
    //
    //     auxiliaryMetrics.appendChild(divAuxiliaryMetric);
    // });

    auxiliaryMetrics.innerHTML = am;

    spanHighChartsSubTitle.appendChild(auxiliaryMetrics);

    return spanHighChartsSubTitle;
}

export default drawChartStats;