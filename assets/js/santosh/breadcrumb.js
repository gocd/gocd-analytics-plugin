import Console from "./Console";

const c = new Console('breadcrumb.js');

class Breadcrumb {

    #dom
    names = [];
    callback
    const

    constructor(callback) {
        // const header = document.getElementById('chart-container-meta');
        //
        // const breadcrumbDiv = document.createElement('div');
        // breadcrumbDiv.setAttribute('id', 'breadcrumb');
        //
        // header.append(breadcrumbDiv);

        this.#dom = document.getElementById('breadcrumb');
        this.callback = callback;
    }

    draw(till) {
        this.#dom.innerHTML = "";

        const totalNames = this.names.length;

        for (let i = 0; i < totalNames; i++) {
            const name = this.names[i];

            c.log('draw name, till ', name.name, till);

            if (name.name === till) {
                const span = document.createElement("span");
                span.textContent = name.caption;
                span.style.color = 'black';
                span.style.backgroundColor = 'lightgrey';

                this.#dom.appendChild(span);

                break;
            }

            const link = document.createElement("a");
            link.setAttribute("id", name.name);
            link.style.color = 'blue';
            // link.style.textDecoration = 'underline';
            // link.style.backgroundColor = 'lightgrey';
            link.style.cursor = 'pointer';

            link.onclick = () => {
                this.callback(link.getAttribute("id"));
            };
            link.textContent = name.caption;
            this.#dom.appendChild(link);

            if (i < totalNames - 1) {
                const divider = document.createElement('span');
                divider.innerText = '/';
                divider.style.marginLeft = '12px';
                divider.style.marginRight = '12px';
                this.#dom.appendChild(divider);
            }
        }

        // this.names.forEach(((name, index) => {
        //
        //     const link = document.createElement("a");
        //     link.setAttribute("id", name.name);
        //     link.style.color = 'blue';
        //     // link.style.textDecoration = 'underline';
        //     link.style.backgroundColor = 'lightgrey';
        //     link.style.cursor = 'pointer';
        //
        //     link.onclick = () => {
        //         this.callback(link.getAttribute("id"));
        //     };
        //     link.textContent = name.caption;
        //     this.#dom.appendChild(link);
        //
        //     if (index < totalNames-1) {
        //         const divider = document.createElement('span');
        //         divider.innerText = '/';
        //         divider.style.marginLeft = '12px';
        //         divider.style.marginRight = '12px';
        //         this.#dom.appendChild(divider);
        //     }
        // }));
    }

    add(name, caption) {

        c.log('breadcrumb add with name, caption', name, caption);

        let found = false;
        for (let i = 0; i < this.names.length; i++) {
            if (this.names[i].name === name) {
                this.names[i].caption = caption;
                found = true;
                break;
            }
        }
        if (!found) {
            this.names.push({name: name, caption: caption});
        }

        c.log('breadcrumb names ', this.names);

        if (this.names.length === 1 || this.names[0].name === name) {
            c.log('breadcrumb only one item, not displaying breadcrumb. returning.');
            this.#dom.innerHTML = "";
            return;
        }

        c.log('now I can show breadcrumb');

        this.draw(name);

    }

}

export default Breadcrumb;