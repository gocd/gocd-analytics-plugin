#!/bin/bash

set -e

###################################################
# This is just a dev deploy script for local envs #
###################################################

export FOR_ENVIRONMENT="development"

if !(id postgres > /dev/null 2>&1); then
  export DB_USER=${DB_USER:-$USER}
fi

gradleArgs=("clean" "test" "integrationTest" "assemble")

for arg in "$@"; do
  case "$arg" in
    --skip-tests)
      echo "Skipping tests" >&2
      gradleArgs=("clean" "assemble")
      ;;
    --prod)
      echo "Building production package" >&2
      export FOR_ENVIRONMENT="production"
      ;;
    *)
      echo "Ignoring argument \"$arg\"" >&2
      ;;
  esac
done

./gradlew "${gradleArgs[@]}"

mkdir -p ../gocd/server/plugins/external && \
  rm -rf ../gocd/server/plugins/external/gocd-analytics-*.jar && \
  cp build/libs/gocd-analytics-*.jar \
     ../gocd/server/plugins/external/
