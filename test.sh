#!/bin/bash

export SERVER_KEY_FILE=./server.jks
export SERVER_KEY_PASS=test123
export SERVER_STORE_PASS=test123
export TRUST_FILE=trust.jks
export TRUST_STORE_PASS=test123
export MONGODB_URI=mongodb://localhost:27017

java -jar ./out/artifacts/fsmtp_v3_jar/fsmtp-v3.jar