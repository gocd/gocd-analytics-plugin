function init(is_settings_header_visible) {
    const settings_title = document.getElementById('settings-title');
    const settings_element = document.getElementById('settings-content');


    settings_title.addEventListener('click', (event) => {
        if(is_settings_header_visible) {
            event.target.textContent = "...";
            settings_element.style.display = "none";
            is_settings_header_visible = false;
        } else {
            event.target.textContent = "Settings";
            settings_element.style.display = "block";
            is_settings_header_visible = true;
        }
    });
}

export {init}