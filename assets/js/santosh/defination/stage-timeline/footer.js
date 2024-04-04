import stageTimelineHeader from "./stage-timeline-header";
import GraphManager from "../../GraphManager";
import {asyncRequest} from "../../utils";
import jobTimelineHeader from "./job-timeline-header";
import pipelinePriorityHeader from "./pipeline-priority-header";
import requestMaster from "../../../RequestMaster";
import priorityDetailsHeader from "./priority-details-header";
import "css/global";
class Footer {

    #dom

    constructor() {

        // const header = document.getElementById('chart-container-meta');
        //
        // const settingsDiv = document.createElement('div');
        // settingsDiv.setAttribute('id', 'settings');
        //
        // header.append(settingsDiv);

        this.#dom = document.getElementById('chart-container-footer');

        this.settings = {
            type: 'Info',
            blink: true
        };

        console.log('1. Footer stage-timeline constructor()');
    }

    showMessage(msg, type, toBlink, dismissAfter) {
        this.#dom.innerHTML = "";
        this.#dom.style.display = 'block';

        const color = this.getColorFromType(type);
        const emoji = this.getEmojiFromType(type);
        const blink_me = toBlink ? "blink_me" : "";
        this.#dom.innerHTML = `
        ${emoji} <span style="color: ${color}" class=${blink_me}>${msg}</span>
        `;
    }

    getColorFromType(type) {
        switch (type) {
            case 'Info':
                return 'blue';
            case 'Warn':
                return 'yellow';
            case 'Error':
                return 'red';
            default:
                return '';
        }
    }

    getEmojiFromType(type) {
        switch (type) {
            case 'Info':
                return 'ℹ️';
            case 'Warn':
                return '⚠️';
            case 'Error':
                return '⛔️';

        }
    }

    clear() {
        this.#dom.innerHTML = "";
        this.#dom.style.display = 'none';
    }


}

export default Footer;