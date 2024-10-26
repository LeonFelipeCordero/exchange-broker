#!/bin/bash

cd ../exchange-market-data/
./gradlew clean build
docker build -f ../exchange-market-data/src/main/docker/Dockerfile.jvm -t exchange-market-data-producer . 

