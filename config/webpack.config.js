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

const webpack = require("webpack");
const path = require("path");
const HtmlBundlerPlugin = require("html-bundler-webpack-plugin");

const rootPath = path.resolve(__dirname, "..");
const assetsPath = path.join(rootPath, "assets");
const outputPath = path.join(rootPath, "build", "resources", "webpack");

const pages = [
  {
    name: "pipeline-instances-chart",
    entrypoint: "pipeline/pipeline-instances-chart.js",
    output_filename: "pipeline-instances-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "stage-build-time-chart",
    entrypoint: "stage/stage-build-time.js",
    output_filename: "stage-build-time-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "longest-waiting-pipelines-chart",
    entrypoint: "analytics-tab/global/longest-waiting-pipelines-chart.js",
    output_filename: "longest-waiting-pipelines-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "agent-with-highest-utilization",
    entrypoint: "analytics-tab/global/agent-with-highest-utilization-chart.js",
    output_filename: "agent-with-highest-utilization-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "info-message",
    entrypoint: "info-message.js",
    output_filename: "info-message.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "agent-state-transition-chart",
    entrypoint: "agent/agent-state-transition-chart.js",
    output_filename: "agent-state-transition-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "workflow-trends-chart",
    entrypoint: "vsm/workflow-trends-chart.js",
    output_filename: "workflow-trends-chart.html",
    based_on_template: "basic-template.html.ejs"
  },
  {
    name: "error-message",
    entrypoint: "error-message.js",
    output_filename: "error-message.html",
    based_on_template: "basic-template.html.ejs"
  }
];

/*
 * Every page outputs a corresponding HTML file (page['output_filename']), generated from its template
 * with the page's JS entrypoint injected via the `entrypoint` template variable.
 *
 * The `?page=` query makes each entry a distinct template instance; without it, pages sharing a
 * template only get shared split chunks (e.g. the production `commons` chunk) injected into the
 * first page's HTML, silently breaking the rest.
 */
function pageEntries() {
  return pages.map(page => ({
    filename: page.output_filename,
    import: `${path.join(assetsPath, "templates", page.based_on_template)}?page=${page.name}`,
    data: {
      title: page.name,
      entrypoint: `@scripts/pages/${page.entrypoint}`,
    }
  }));
}

module.exports = (env = {}, argv = {}) => {
  const mode = argv.mode || process.env.NODE_ENV || "development";
  const isProduction = mode === "production";

  return {
    mode,
    context: rootPath,
    target: "web",

    output: {
      assetModuleFilename: isProduction ? "assets/[name].[contenthash:8][ext][query]" : "assets/[name][ext][query]",
      path: outputPath,
      clean: true
    },

    plugins: [
      new HtmlBundlerPlugin({
        entry: pageEntries(),
        minify: "auto", // minify HTML in production mode only, as html-webpack-plugin did
        js: {
          filename: isProduction ? "js/[name].[contenthash:8].js" : "js/[name].js",
          chunkFilename: isProduction ? "js/[name].[contenthash:8].chunk.js" : "js/[name].chunk.js",
          //inline: true, // enable to inline JS into the HTML
        },
        css: {
          filename: isProduction ? "css/[name].[contenthash:8].css" : "css/[name].css",
          chunkFilename: isProduction ? "css/[name].[contenthash:8].chunk.css" : "css/[name].chunk.css",
          //inline: true, // enable to inline CSS into the HTML
        },
        loaderOptions: {
          preprocessor: "ejs",
          sources: [
            {
              tag: "script",
              attributes: ["src"],
              // Scripts served by the GoCD server itself at runtime must be left as-is,
              // not resolved/bundled by webpack.
              filter: ({ attribute, value }) => {
                const staticFilenames = [
                  "gocd-server-comms.js",
                  "highcharts.src.js",
                  "no-data-to-display.src.js",
                  "xrange.src.js",
                ];
                if ("src" === attribute && staticFilenames.some(file => value.includes(file))) {
                  return false;
                }
              },
            },
          ]
        },
      }),

      new webpack.IgnorePlugin({
        resourceRegExp: /^\.\/locale$/,
        contextRegExp: /moment$/
      }),
    ],

    resolve: {
      alias: {
        "@scripts": path.join(rootPath, "assets/js"),
      },
      extensions: [".js", ".css", ".scss"],
      modules: [assetsPath, "node_modules"],
      fallback: {
        fs: false
      }
    },

    externals: {
      "gocd-server-comms": "AnalyticsEndpoint",
      "highcharts-shim": "Highcharts",
    },

    module: {
      rules: [
        {
          test: /\.s?[ac]ss$/,
          use: [
            "css-loader",
            {
              loader: "sass-loader",
              options: {
                sassOptions: {
                  // Hide deprecation warnings from highcharts' bundled SCSS; migrating our own
                  // @import usage to @use is a separate task from the webpack 5 upgrade.
                  quietDeps: true,
                  silenceDeprecations: ["import"]
                }
              }
            }
          ]
        },
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              plugins: ["lodash"],
              presets: [["@babel/preset-env", {modules: false}]],
              cacheDirectory: true
            }
          }
        },
        {
          test: /\.(svg|png|jpe?g|gif)$/i,
          type: "asset/resource",
          //type: "asset/inline", // HtmlBundlerPlugin embedds inline assets into CSS
        }
      ]
    },

    stats: "minimal"
  };
};
