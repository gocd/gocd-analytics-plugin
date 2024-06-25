import {Litepicker} from 'litepicker';

export class DateManager {

    constructor() {
    }

    async addDatelitePickerDiv(selector, callback) {
        console.log("passed selector", selector);

        let newDiv = document.createElement("input");
        newDiv.setAttribute("id", "litepicker");

        const today = new Date();

        const picker = new Litepicker({
            // element: document.getElementById('litepicker'),
            element: newDiv,
            singleMode: false,
            // autoRefresh: true,
            // startDate: new Date(today.getFullYear(), today.getMonth(), 1),
            // maxDate: today,
            setup: (picker) => {
                picker.on("selected", (date1, date2) => {
                    console.log("setup selected");
                    callback(date1, date2);
                });
            },
        });

        selector.innerHTML = newDiv;

        return picker;
    }

}