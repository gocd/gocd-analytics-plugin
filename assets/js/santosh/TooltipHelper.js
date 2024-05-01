import {secondsToHms} from "./utils";
import TooltipManager from "./TooltipManager";

function getPipelinePriorityTooltipFormatterFunction(xSeriesName) {
    return (params) => {
        const tooltip = new TooltipManager();
        tooltip.addTitle(params[0].name);
        params.forEach(item => {
            let value = null;
            if (item.seriesName === xSeriesName) {
                value = item.value + 'x';
            } else {
                value = secondsToHms(item.value);
            }
            tooltip.addItem(item.marker, item.seriesName, value);
        });
        tooltip.addFooter('Click to see the specific details');
        return tooltip.getStandard();
    }
}

export default getPipelinePriorityTooltipFormatterFunction;