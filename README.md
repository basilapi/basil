# BASIL #

![Build JDK 1.8](https://github.com/basilapi/basil/actions/workflows/main.yml/badge.svg)
![Build Java 11](https://github.com/basilapi/basil/actions/workflows/mvn-Java11.yml/badge.svg)
![Build Java 17](https://github.com/basilapi/basil/actions/workflows/mvn-Java17.yml/badge.svg)

BASIL is designed as middleware system that mediates between SPARQL endpoints and applications.

With BASIL you can build Web APIs on top of SPARQL endpoints.

BASIL stores SPARQL queries and builds APIs with standard and customizable formats.

## Run ##
Latest runnable jar can be downloaded from the releases section.

### TDB2 backend (default)
An example configuration file can be found in the project root folder (`basil-tdb2.ini`).
An example log4j configuration file is at `server/src/test/resources/log4j.xml`.

Execute:

```
$ java -jar -Dbasil.configurationFile=./basil-tdb2.ini -Dlog4j.configurationFile=./log4j2.xml basil-server-VERSION.jar -p 8080
#1: welcome to the world's helthiest food
#2: basil is starting on port 8080
#3: done
#4: enjoy
```

### MySQL backend (legacy)
You need to:

 - Have a MySQL server.
 - Prepare a database running the [db.sql](db.sql) queries (at the root of the codebase).
 - Prepare the configuration file (the connection parameters), see [this file](basil.ini) as an example.
 - Prepare a log4j2 configuration file (if you want logging). See [this file](server/src/test/resources/log4j2.xml) as an example.

When ready, execute:

```
$ java -jar -Dbasil.configurationFile=../basil.ini -Dlog4j.configurationFile=src/test/resources/log4j2.xml basil-server-0.3.0.jar -p 8080
#1: welcome to the world's helthiest food
#2: basil is starting on port 8080
#3: done
#4: enjoy
```
## Usage ##
BASIL is a system based on a Web API for creating and managing a stack of Web APIs on top SPARQL endpoints. 
On its own, BASIL does not come with a graphic user interface. Instead, it can be fully controlled via its API.
[See the cURL tutorial for details](https://github.com/basilapi/basil/wiki/cURL-tutorial).

## Build ##
The basil project is managed and built with Maven.

```
mvn clean install
```
Note: to also run tests, you need an active internet connection (as they use public SPARQL endpoints).
If you want to skip tests, you can:

```
mvn install -DskipTests
```

## Releasing ##
The following command will pack a release, sign the artefacts, and push them to maven central.
```
mvn deploy -DperformRelease=true
```