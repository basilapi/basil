#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd $DIR
java -jar -Dbasil.configurationFile=../basil.ini -Dlog4j.configurationFile=src/test/resources/log4j2.xml  target/basil-server-0.3.0-SNAPSHOT.jar -p 8080


