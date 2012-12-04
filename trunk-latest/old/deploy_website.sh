#!/bin/bash
export PATH=$PATH:$(pwd)/../libosm/maven/bin
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_04
./libosm/maven/bin/mvn site-deploy
#../libosm/maven/bin/mvn site
