/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const path = require("path"),
   webpack = require("webpack"),
     merge = require("webpack-merge").merge;

const webpackConfig = require('./webpack.config.js')();

webpackConfig.mode = "development";
webpackConfig.externals = { };

process.env.CHROME_BIN = require("puppeteer").executablePath();

module.exports = function (config) {
  config.set({
    plugins: [
      require("karma-webpack"),
      require("karma-tap"),
      require("karma-chrome-launcher"),
      require("karma-tap-pretty-reporter")
    ],
    basePath: path.resolve(__dirname, ".."),
    frameworks: ["tap"],

    files: [
      "spec/javascripts/all-tests.bundle.js",
    ],

    preprocessors: {
      "spec/javascripts/all-tests.bundle.js": ["webpack"]
    },

    reporters: ["tap-pretty"],

    tapReporter: {
      prettify: require("tap-diff"),
      separator: "****************************"
    },

    webpack: merge(webpackConfig, {
      plugins: [
        new webpack.DefinePlugin({
          __TEST_DIR__: JSON.stringify("."),
          __FILE_RGX__: /^.+-(spec|test)\.js$/
        })
      ]
    }),
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ["ChromeHeadlessNoSandbox"],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },
    singleRun: false,
    concurrency: Infinity,
    client: {
      captureConsole: false
    },
  });
};
