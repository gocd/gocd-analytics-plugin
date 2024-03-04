const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = {
    entry: './assets/js/pages/analytics-tab/global/longest-waiting-pipelines-chart.js', output: {
        path: path.resolve(__dirname, 'dist'), filename: 'assets.js',
    },
     module: {
         rules: [
             {
                 test: /\.s?[ac]ss$/,
                 use: [
                     MiniCssExtractPlugin.loader,
                     'css-loader',
                     'sass-loader'
                 ]
             },
             {
                 test: /\.js$/,
                 exclude: /node_modules/,
                 use: [
                     {
                         loader: 'babel-loader',
                         options: {
                             plugins: ['lodash'],
                             presets: [[ '@babel/preset-env', { "modules": false } ]]
                         }
                     }
                 ],
             },
             {
                 test: /\.(svg|png|jpg|gif)$/,
                 use: ['file-loader']
             }
         ]
     },
    resolve: {
        extensions: ['.js', '.css', '.scss'],
        modules: [path.join(__dirname, 'assets'), 'node_modules']
    },
    externals: {
//          'gocd-server-comms': 'AnalyticsEndpoint',
//          'highcharts-shim': 'Highcharts'
        },
    optimization: {
        minimize: false, // You can set this to false for development
    },
};