#! /bin/sh

# This script builds the GoCD analytics plugin and copies the
# latest JAR file to the server's plugins directory.

# Exit immediately if a command exits with a non-zero status.
set -e

# --- READ ME ---
# Fill this before running the script.
WORKSPACE="/Users/santoshkumarbachar/Workspace"


ANALYTICS_PLUGIN_PWD="${WORKSPACE}/gocd-analytics-plugin"
SERVER_PLUGINS="${WORKSPACE}/gocd/server/plugins/external"

# The following variables are derived from the above and should not be changed.
ANALYTICS_PLUGIN_LIBS="${ANALYTICS_PLUGIN_PWD}/build/libs"

echo "Building the GoCD analytics plugin..."

# Execute the Gradle build command.
# The double quotes around the path handle any spaces in the path.
# We also check for the command's success.
cd "$ANALYTICS_PLUGIN_PWD"
if ! "${ANALYTICS_PLUGIN_PWD}/gradlew" build --info; then
  echo "Error: Gradle build failed. Exiting."
  exit 1
fi

echo "Build complete. Locating the latest JAR file."

# Change to the libs directory and check if the directory exists.
if [ ! -d "$ANALYTICS_PLUGIN_LIBS" ]; then
    echo "Error: Plugin library directory not found: ${ANALYTICS_PLUGIN_LIBS}. Exiting."
    exit 1
fi

# Find the newest JAR file in the directory.
# This uses 'ls -t' to sort by time and 'head -n1' to get the latest file.
cd "$ANALYTICS_PLUGIN_LIBS"
file="$(ls -t ./gocd-analytics-plugin-*.jar | head -n1)"

# Check if a file was found before proceeding.
if [ -z "$file" ]; then
    echo "Error: No plugin JAR file found in ${ANALYTICS_PLUGIN_LIBS}. Exiting."
    exit 1
fi

echo "Found latest plugin file: $file"
echo "Removing any old versions from the server plugins directory."

# Remove any existing JAR files with the same name.
rm -f "${SERVER_PLUGINS}/gocd-analytics-plugin-*.jar"

echo "Copying $file to $SERVER_PLUGINS."

# Copy the new JAR file.
cp "$file" "$SERVER_PLUGINS"

echo "✅ Done! The new plugin has been deployed. Please reload the GoCD server page."
