# GoCD Analytics Plugin

Table of Contents
=================

  * [Prerequisites](#prerequisites)
  * [Installation](#installation)

## Prerequisites

### GoCD Server

- GoCD server version **20.5.0** or higher is needed.

### Highcharts JS

Highcharts JS is a Javascript charting library. It is used by the GoCD analytics plugin. Unfortunately Highcharts JS is incompatible with any open-source license and due to that reason it cannot be distributed along with the plugin. Highcharts is distributed by [Highsoft](https://shop.highsoft.com/) and you need to get a license from them before using it.

If you have a license or have some reason to believe that you don't need one, you can get the required Javascript files from the locations below:

https://code.highcharts.com/6.1.3/modules/no-data-to-display.src.js
https://code.highcharts.com/6.1.3/modules/xrange.src.js
https://code.highcharts.com/6.1.3/highcharts.src.js

This plugin is known to work with the three files mentioned above (version 6.1.3 of Highcharts JS). Once you get those files, they need to be placed in a directory called `analytics-assets` in GoCD's server directory. It is usually `/var/lib/go-server` on Linux and `C:\Program Files (x86)\Go Server` on Windows. See the [GoCD installation documentation](https://docs.gocd.org/current/installation/installing_go_server.html) for your operating system to find the right directory.

Once the files mentioned above are placed in the directory `analytics-assets`, the tree structure will look like this:

```
$ cd /var/lib/go-server/; tree .
.
├── LICENSE
├─ analytics-assets
│   ├── highcharts.src.js
│   ├── no-data-to-display.src.js
│   └── xrange.src.js
├── artifacts
├── bin
├── config
├── cruise.war
├── db
├── felix-cache
├── lib
├── logs
├── plugins
├── plugins_work
├── run
├── work
├── wrapper
└── wrapper-config
```

### PostgreSQL

- This plugin requires PostgreSQL `v9.6`.

- This plugin requires an **empty** PostgreSQL database for its data. If you are using the GoCD PostgreSQL Addon, please note that the database schema that you use for analytics data should be separate from the schema used by GoCD.

- The plugin requires the [citext module](https://www.postgresql.org/docs/9.6/static/citext.html) for creating the schema. Install the `postgresql-contrib` packages to include the citext module.

- **Creating the citext extension:**

  1. If the DB user you are going to configure the plugin with already has `superuser` or `rds_superuser` (Amazon RDS) privileges, the `citext` extension will automatically be created through a pre-packaged migration upon configuring the plugin.

  2. If the DB user you are going to configure the plugin with is just a user of the analytics database, but not a `superuser`, then the `citext` extension will have to be created manually. To do this, connect to the PostgreSQL server as a user with 'superuser' privileges and run `CREATE EXTENSION IF NOT EXISTS citext;` on the empty database to be used for the analytics plugin.

- **Example database user creation:**

  On Amazon RDS:

    ```sql
    CREATE ROLE "gocd_analytics_user" PASSWORD 'gocd_analytics_password' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;
    CREATE DATABASE "gocd_analytics";
    GRANT ALL PRIVILEGES ON DATABASE "gocd_analytics" TO "gocd_analytics_user";
    GRANT rds_superuser TO "gocd_analytics_user";
    ```

  On a normal Postgres server instance:

    ```sql
    CREATE ROLE "gocd_analytics_user" PASSWORD 'gocd_analytics_password' NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;
    CREATE DATABASE "gocd_analytics";
    GRANT ALL PRIVILEGES ON DATABASE "gocd_analytics" TO "gocd_analytics_user";
    ALTER ROLE "gocd_analytics_user" SUPERUSER;
    ```

## Installation

 - Copy the file `build/libs/gocd-analytics-plugin-VERSION.jar` (or the one downloaded from [GitHub Releases](https://github.com/gocd/gocd-analytics-plugin/releases)) to the GoCD server under `${GO_SERVER_DIR}/plugins/external` and restart the server.
 - The `GO_SERVER_DIR` is usually `/var/lib/go-server` on **Linux** and `C:\Program Files (x86)\Go Server` on **Windows**.

<hr>

Once the plugin is installed and the server is restarted, the plugin [can be configured](./Configure.md).
