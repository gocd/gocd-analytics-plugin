#! /bin/sh

#FOR_ENVIRONMENT=webpack-dev

./gradlew build --info

cd ./build/libs

file=`ls -t ./gocd-analytics-plugin*.jar | head -n1`
rm /Users/santoshkumarbachar/Workspace/gocd/server/plugins/external/gocd-analytics-plugin*.jar

cp $file /Users/santoshkumarbachar/Workspace/gocd/server/plugins/external

echo "Ready for testing. Reload the page."
