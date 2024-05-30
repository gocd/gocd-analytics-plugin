import {addOptionsToSelect} from "./utils";


async function setOrderSelector(requestOrderSelector) {
    await addOptionsToSelect(requestOrderSelector, [{text: "Ascending", value: "ASC"}, {
        text: "Descending",
        value: "DESC"
    }]);
}

export {
    setOrderSelector
};
