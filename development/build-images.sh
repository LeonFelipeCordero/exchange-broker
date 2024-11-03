#!/bin/bash

cd ../exchange-market-data/
./gradlew clean build -X tests
docker build -f ../exchange-market-data/src/main/docker/Dockerfile.jvm -t exchange . 

cd ../mini-broker/
docker build -t broker .

