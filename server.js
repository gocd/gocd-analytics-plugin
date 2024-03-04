const express = require('express');
const path = require('path');
const app = express();

app.use('/plugin-endpoint.js', express.static(path.join(__dirname, 'assets', 'plugin-endpoint.js')));
app.use('/gocd-server-comms.js', express.static(path.join(__dirname, 'assets', 'gocd-server-comms.js')));
app.use('/assets.js', express.static(path.join(__dirname, 'dist', 'assets.js')));

app.get('/', function (req, res) {
    res.sendFile(path.join(__dirname, '/dev.html'));
});

const port = 8080;
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});