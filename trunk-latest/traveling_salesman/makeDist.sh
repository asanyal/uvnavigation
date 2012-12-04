#!/bin/bash
./build/apache-ant-1.7.0/bin/ant -Dsshpass=$1  -Dkeypass=$2 -Dkeystorepass=$3 uploaddist
