#!/bin/bash

server="server/target/"$(cd server/target && ls|grep basil-server)
echo "Launching version "$server
java -jar -Dbasil.configurationFile=basil-tdb2.ini -Dlog4j.configurationFile=server/src/test/resources/log4j2.xml $server -p 8080

