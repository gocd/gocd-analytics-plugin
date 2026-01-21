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

const webpack = require('webpack');
const path = require('path');
const _ = require('lodash');
const HtmlBundlerPlugin = require('html-bundler-webpack-plugin');

const pages = [
  {
    name: 'pipeline-instances-chart',
    entrypoint: 'pipeline/pipeline-instances-chart.js',
    output_filename: 'pipeline-instances-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'stage-build-time-chart',
    entrypoint: 'stage/stage-build-time.js',
    output_filename: 'stage-build-time-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'longest-waiting-pipelines-chart',
    entrypoint: 'analytics-tab/global/longest-waiting-pipelines-chart.js',
    output_filename: 'longest-waiting-pipelines-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'agent-with-highest-utilization',
    entrypoint: 'analytics-tab/global/agent-with-highest-utilization-chart.js',
    output_filename: 'agent-with-highest-utilization-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'info-message',
    entrypoint: 'info-message.js',
    output_filename: 'info-message.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'agent-state-transition-chart',
    entrypoint: 'agent/agent-state-transition-chart.js',
    output_filename: 'agent-state-transition-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'workflow-trends-chart',
    entrypoint: 'vsm/workflow-trends-chart.js',
    output_filename: 'workflow-trends-chart.html',
    based_on_template: 'basic-template.html.ejs'
  },
  {
    name: 'error-message',
    entrypoint: 'error-message.js',
    output_filename: 'error-message.html',
    based_on_template: 'basic-template.html.ejs'
  }
];

/* Every page outputs a corresponding HTML file (page['output_filename']). */
module.exports = (env) => {
  return {
    output: {
      path: path.resolve(__dirname, '..', 'build', 'resources', 'webpack')
    },

    plugins: _.flattenDeep([
      new HtmlBundlerPlugin({
        entry: _.map(pages, function (page) {
          return {
            import: path.resolve(__dirname, '..', 'assets', 'templates', page['based_on_template']),
            filename: page['output_filename'],
            data: {
              title: page['name'],
            },
          };
        }),
        js: {
          filename: "js/[name].js",
        },
        css: {
          filename: "[name]-[contenthash].css",
          chunkFilename: "[name]-[chunkhash].css"
        }
      }),
      new webpack.IgnorePlugin({ resourceRegExp: /^\.\/locale$/, contextRegExp: /moment$/}),
    ]),

    optimization: {
      splitChunks: {
        cacheGroups: {
          styles: {
            name: 'styles',
            test: /\.s?[ac]ss$/,
            chunks: 'all',
            enforce: true
          }
        }
      }
    },

    /* Look at 'assets' directory as well for JS and CSS files when resolving in 'import' statements. */
    resolve: {
      alias: {
        '@scripts': path.join(__dirname, 'src/assets/js'),
        '@styles': path.join(__dirname, 'src/assets/css'),
      },
      extensions: ['.js', '.css', '.scss'],
      modules: [path.join(__dirname, '..', 'assets'), 'node_modules']
    },

    externals: {
      'gocd-server-comms': 'AnalyticsEndpoint',
      'highcharts-shim': 'Highcharts'
    },

    node: {
      fs: "empty"
    },

    module: {
      rules: [
        {
          test: /\.s?[ac]ss$/,
          use: [
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
    }
  };
};
