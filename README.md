# BASIL #
BASIL is designed as middleware system that mediates between SPARQL endpoints and applications.

With BASIL you can build Web APIs on top of SPARQL endpoints.

BASIL stores SPARQL queries and builds APIs with standard and customizable formats.

## Build with Maven ##

```
#!shell

$ mvn install
```

## Run ##

For example:

```
$ java -jar -Dbasil.configurationFile=../basil.ini -Dlog4j.configurationFile=src/test/resources/log4j2.xml basil-server-0.3.0.jar -p 8080
#1: welcome to the world's helthiest food
#2: basil is starting on port 8080
#3: done
#4: enjoy
```


