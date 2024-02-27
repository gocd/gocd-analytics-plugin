function showLegend() {
    const myDiv = document.getElementById("chart-container-footer");
    myDiv.innerHTML = "";

    const legend = document.createElement("div");
    legend.classList.add("legend");

    const statuses = ['Idle', 'Building', 'Cancelled', 'Missing', 'LostContact', 'Unknown'];

    statuses.forEach(status => {

        const legendItem = document.createElement("div");
        legendItem.classList.add("legend-item");

        const legendDot = document.createElement("span");
        legendDot.classList.add("legend-dot");
        legendDot.classList.add(status.toLowerCase());

        const legendText = document.createElement("span");
        legendText.classList.add("legend-text");
        legendText.innerText = status.toString();

        legendItem.appendChild(legendDot);
        legendItem.appendChild(legendText);

        legend.appendChild(legendItem);
    });


    myDiv.prepend(legend);

}

export default showLegend;