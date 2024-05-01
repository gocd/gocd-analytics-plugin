import {secondsToHms} from "./utils";

class TooltipManager {
    title = null;
    content = [];
    footer = null;

    addTitle(title) {
        this.title = title;
    }

    addContent(content) {
        this.content.push(content);
    }

    addItem(marker, key, value) {
        this.addContent([marker, key, value]);
    }

    addFooter(footer) {
        this.footer = footer;
    }

    getStandard() {
        let ret = this.title + '<br>';
        this.content.forEach(item => {
            ret += `<div><span style="margin-right: 6px;">${item[0]} ${item[1]}</span> <span style="float: right; font-weight: bold;">${item[2]}</span></div>`;
        });
        if (this.footer) {
            ret += `<hr><i>${this.footer}</i>`;
        }
        return ret;
    }
}

export default TooltipManager;