import {Litepicker} from 'litepicker';
export class DateManager {

    #chartDom
    #datepicker

    constructor(chartDom) {
        this.#chartDom = chartDom;
        this.addDatelitePickerDiv();
    }

    addDatelitePickerDiv() {
        let newDiv = document.createElement("div");
        newDiv.setAttribute("id", "litepicker");

        this.#chartDom.prepend(newDiv);

        const picker = new Litepicker({
            element: document.getElementById('litepicker'),
            singleMode: false,
        });

        this.#datepicker = picker;
        console.log('datelite added ', this.#datepicker);
    }

    showDatelitePicker() {
        console.log("showDatelitePicker() clicked");
        console.log('datelite is ', this.#datepicker);
        this.#datepicker.show();
    }
}