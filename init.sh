#!/bin/sh

git clone git@github.com:apache/flink-connector-aws.git

pushd flink-connector-aws
  mvn clean install -Dmaven.test.skip=true -T1C
popd

git clone git@github.com:sergejsvisockis/book-manager.git

pushd book-manager
  mvn clean install -DskipTests -T1C
popd

mvn -U clean install