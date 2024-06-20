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

function getLabelTruncateAwareTooltipFormatterFunction(actualCategories) {
    return (params) => {
        const tooltip = new TooltipManager();
        const extractedTitle = actualCategories[params[0].dataIndex];
        if (Array.isArray(extractedTitle)) {
            if (extractedTitle.length > 2) {
                tooltip.addTitle(extractedTitle[0] + ' >> ' + extractedTitle[1] + ' >> ' + extractedTitle[2]);
            } else {
                tooltip.addTitle(extractedTitle[0] + ' >> ' + extractedTitle[1]);
            }
        } else {
            tooltip.addTitle(extractedTitle);
        }
        params.forEach(item => {
            tooltip.addItem(item.marker, item.seriesName, secondsToHms(item.value));
        });
        tooltip.addFooter('Click to see the specific details');
        return tooltip.getStandard();
    }
}

export {getPipelinePriorityTooltipFormatterFunction, getLabelTruncateAwareTooltipFormatterFunction};