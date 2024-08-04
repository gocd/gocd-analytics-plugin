import {Litepicker} from 'litepicker';
import {formatDatePicker} from "./utils";

export class DateManager {

    constructor() {
    }

    async addDatelitePickerDiv(selector, callback) {
        console.log("passed selector", selector);

        let newDiv = document.createElement("input");
        newDiv.setAttribute("id", "litepicker");

        selector.style.color = "blue";
        selector.style.cursor = "pointer";

        const today = new Date();

        const picker = new Litepicker({
            // element: document.getElementById('litepicker'),
            element: newDiv,
            singleMode: false,
            // autoRefresh: true,
            // startDate: new Date(today.getFullYear(), today.getMonth(), 1),
            maxDate: today,
            setup: (picker) => {
                picker.on("selected", (date1, date2) => {
                    console.log("setup selected");
                    const formattedDate = `${formatDatePicker(date1)} - ${formatDatePicker(date2)}`
                    selector.textContent = formattedDate;
                    callback(date1, date2);
                });
            },
        });

        selector.innerHTML = newDiv;

        return picker;
    }

}