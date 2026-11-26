# BASIL #

![Build Java 21](https://github.com/basilapi/basil/actions/workflows/mvn-Java21.yml/badge.svg)

BASIL is designed as middleware system that mediates between SPARQL endpoints and applications.

With BASIL you can build Web APIs on top of SPARQL endpoints.

BASIL stores SPARQL queries and builds APIs with standard and customizable formats.

## Run ##
BASIL requires **Java 21** or later.

The runnable jar (`basil-server-VERSION.jar`) can be downloaded from the [releases](https://github.com/basilapi/basil/releases) page, together with an example configuration (`basil-server.ini`) and a log4j2 configuration (`log4j2.xml`).

### TDB2 backend (default)
The example configuration [`basil-server.ini`](basil-server.ini) stores data in `./basil-data`. A log4j2 configuration is at [`server/src/test/resources/log4j2.xml`](server/src/test/resources/log4j2.xml).

```
$ java -jar -Dbasil.configurationFile=./basil-server.ini -Dlog4j.configurationFile=./log4j2.xml basil-server-VERSION.jar -p 8080
#1: welcome to the world's helthiest food
#2: basil is starting on port 8080
#3: resources setup
#4: enjoy
```

Parameters used as SERVICE IRI (`SERVICE ?_endpoint_iri { ... }`) are disabled by default; enable them with `-Dbasil.sparql.serviceParameters=true` (the caller then chooses which endpoint is queried).

### MySQL backend (deprecated)
You need to:

 - Have a MySQL server.
 - Prepare a database running the [db.sql](db.sql) queries (at the root of the codebase).
 - Prepare the configuration file (the connection parameters), see [this file](basil.ini.default) as an example.
 - Prepare a log4j2 configuration file (if you want logging), see [this file](server/src/test/resources/log4j2.xml) as an example.

When ready, execute:

```
$ java -jar -Dbasil.configurationFile=./basil.ini -Dlog4j.configurationFile=./log4j2.xml basil-server-VERSION.jar -p 8080
```

## Usage ##
BASIL is a system based on a Web API for creating and managing a stack of Web APIs on top SPARQL endpoints.
On its own, BASIL does not come with a graphic user interface. Instead, it can be fully controlled via its API.
[See the cURL tutorial for details](https://github.com/basilapi/basil/wiki/cURL-tutorial).
The naming convention for API parameters is described in the [wiki](https://github.com/basilapi/basil/wiki/SPARQL-variable-name-convention-for-WEB-API-parameters-mapping).

## Build ##
The basil project is managed and built with Maven (Java 21).

```
mvn clean install
```

The integration tests (profile `it`, active by default) start a local Fuseki and a BASIL server; some tests use public SPARQL endpoints and need an internet connection. To skip them:

```
mvn clean install -P '!it'
```

To skip all tests:

```
mvn clean install -DskipTests
```

## Releasing ##

1. Set the release version in all poms (they are marked with `<!-- basil version -->`), build, commit and tag:

   ```
   grep -rl --include=pom.xml '1.0-DEV<!-- basil version -->' . | xargs sed -i '' 's/1.0-DEV<!-- basil version -->/X.Y.Z<!-- basil version -->/'
   mvn clean install
   git commit -am "Release X.Y.Z" && git tag vX.Y.Z
   ```

2. Sign and deploy to Maven Central, then push the tag. The `server` module is excluded: its artifact is the runnable jar (~65 MB), which is distributed with the GitHub release instead (Maven Central applies a monthly size limit per organisation).

   ```
   mvn deploy -DperformRelease=true -DskipTests -pl '!server'
   git push origin vX.Y.Z
   ```

3. Create the GitHub release with the runnable jar and the example configuration:

   ```
   gh release create vX.Y.Z server/target/basil-server-X.Y.Z.jar basil-server.ini server/src/test/resources/log4j2.xml --verify-tag --title "BASIL X.Y.Z" --notes-file notes.md
   ```

   (add `--prerelease` for release candidates)

4. Set the development version back (`X.Y.Z` → `1.0-DEV`), commit and push.


