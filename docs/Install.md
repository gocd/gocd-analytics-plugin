# GoCD Analytics Plugin

Table of Contents
=================

  * [Prerequisites](#prerequisites)
  * [Installation](#installation)

## Prerequisites

### GoCD Server

- The GoCD server version **19.8.0** or higher.

### Postgres

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

 - Copy the file `build/libs/gocd-analytics-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external`
and restart the server.
 - The `GO_SERVER_DIR` is usually `/var/lib/go-server` on **Linux** and `C:\Program Files\Go Server` on **Windows**.
