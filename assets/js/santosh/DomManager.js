import {DateManager} from "./DateManager";

export class DomManager {
    #chartDom
    #dateManager
    #level
    #links

    constructor(name) {
        this.initialiseCustomChartDom();
        // this.#dateManager = new DateManager(this.#chartDom);
        this.#level = 0;
        this.#links = [name]
    }

    initialiseCustomChartDom() {
        this.#chartDom = document.getElementById('chart-container-meta');
    }

    getChartDom() {
        return this.#chartDom;
    }

    incrementLevel() {
        this.#level += 1;
    }

    decrementLevel() {
        this.#level -= 1;
    }

    addNavigationHeader() {
        let div = document.createElement("div");
        div.setAttribute("id", "header");
        div.innerText = "Navigation header";

        let button = document.createElement("button");
        button.innerText = "Show date picker";
        button.addEventListener('click', () => {
            this.#dateManager.showDatelitePicker();
        })
        div.appendChild(button);

        this.#chartDom.prepend(div);
    }

    reconstructHeader() {
        let div = document.getElementById("header");

        this.#links.forEach(l => {
            div.innerHTML = `
            <a href="javascript:alert('');">${l}</a> /
            `;
        });
    }

    addLink(name) {
        this.#links.push(name);
        this.reconstructHeader();
        this.incrementLevel();
    }

    removeLink() {
        this.#links.pop();
        this.reconstructHeader();
        this.decrementLevel();
    }

    addBackButton() {
        const chartDom = this.#chartDom.getChartDom();

        const backButton = createBackButton();

        this.addDatelitePickerDiv();

        backButton.addEventListener('click', () => {
            console.log("back button clicked, and going to level ", this.level - 1);
            console.log("now will show graphs for the title ", this.dataStore[this.level - 1].title);

            this.restoreGraphDataToPreviousState();
            this.generate(true);
        });

        function clearBackButton() {
            let div = document.getElementById("divBack");
            if (div !== null) {
                div.innerText = "";
            }
        }

        function createBackButton() {
            clearBackButton();

            let newButton = document.createElement("button");
            newButton.innerText = "< Back";

            let newDiv = document.createElement("div");
            newDiv.setAttribute("id", "divBack");
            newDiv.appendChild(newButton);
            newDiv.innerHTML = "<b>Navigation:</b> Pipeline: <a href=''>pipeline name</a> &gt; Stage: <a href=''>stage name</a>";

            chartDom.prepend(newDiv);

            return newButton;
        }

    }
}