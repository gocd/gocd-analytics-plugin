#! /bin/sh

#FOR_ENVIRONMENT=webpack-dev

./gradlew build

cd ./build/libs

file=`ls -t ./gocd-analytics-plugin*.jar | head -n1`
cp $file /Users/santoshbachar/dev/gocd/server/plugins/external

echo "Ready for testing. Reload the page."
