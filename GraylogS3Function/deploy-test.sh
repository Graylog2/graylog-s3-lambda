#!/usr/bin/env bash
echo "*** Packaging up function ***"
mvn package;

echo "*** Uploading Jar to Lambda ***"
aws lambda update-function-code --function-name integrations-s3-test2 --zip-file fileb://target/HelloWorld-1.0.jar;

./test.sh;