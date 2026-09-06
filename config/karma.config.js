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
 { merge } = require("webpack-merge");

const webpackConfig = require("./webpack.config.js")();

webpackConfig.mode = "development";
webpackConfig.externals = { };

// karma-webpack provides its own entry/output; generating the chart HTML pages and writing to the
// build output directory don't apply when bundling specs.
webpackConfig.plugins = webpackConfig.plugins.filter(plugin => plugin.constructor.name !== "HtmlBundlerPlugin");
delete webpackConfig.output;

module.exports = async function (config) {
  process.env.CHROME_BIN = await require("puppeteer").executablePath();
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
      prettify: require("faucet"),
      separator: "****************************"
    },

    webpack: merge(webpackConfig, {
      plugins: [
        new webpack.DefinePlugin({
          __TEST_DIR__: JSON.stringify("."),
          __FILE_RGX__: /^.+-(spec|test)\.js$/
        }),
        new webpack.ProvidePlugin({
          Buffer: ["buffer", "Buffer"],
          process: "process/browser.js"
        })
      ],
      resolve: {
        fallback: {
          // tape requires these node builtins, which webpack 5 no longer polyfills automatically
          path: require.resolve("path-browserify"),
          stream: require.resolve("stream-browserify"),
          buffer: require.resolve("buffer/"),
          events: require.resolve("events/")
        }
      }
    }),
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ["ChromeHeadlessNoSandbox"],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: "ChromeHeadless",
        flags: ["--no-sandbox"]
      }
    },
    singleRun: false,
    concurrency: Infinity,
    client: {
      captureConsole: false
    },
  });
};
