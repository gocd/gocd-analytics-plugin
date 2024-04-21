class Console {

    dev = true
    prod = false

    constructor(source, mode = 'prod') {
        this.source = source;
        this.mode = mode;

        if (this.mode === 'prod') {
            this.dev = false
            this.prod = true
        }

        //   console.log('this.source, this.mode', this.source, this.mode);

    }

    log(...msg) {
        if (this.dev === true) {
            console.log(this.source, ' ', msg.join(''));
        }
    }

}

export default Console;