#!/bin/bash

cd ../exchange-market-data/
./gradlew clean build -x test
docker build -f ../exchange-market-data/src/main/docker/Dockerfile.jvm --no-cache -t exchange . 

cd ../mini-broker/
docker build --no-cache -t broker .

