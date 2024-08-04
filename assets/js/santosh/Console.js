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

    isDevMode() {
        return this.dev === true;
    }

    log(message, data = null, level = 'info') {
        if (this.isDevMode()) {
            const levelPrefix = `[${level.toUpperCase()}]`;
            let messageToLog = `${levelPrefix} ${message}`;

            if (data != null) {
                if (typeof data === 'object') {
                    // messageToLog += ` ${JSON.stringify(data)}`;
                    messageToLog += ` ${data}`;
                } else {
                    messageToLog += ` ${data}`;
                }
            }

            console.log(messageToLog);
        }
    }

    logs(...msg) {
        if (this.dev === true) {
            console.log(this.source, ' ', msg.join(''));
        }
    }

}

export default Console;