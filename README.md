# BASIL #
Tool for building Web APIs on top of SPARQL endpoints.

## Build with Maven ##

```
#!shell

$ mvn install
```

## Run ##
```
$ java -jar basil-server-0.2.0-SNAPSHOT.jar -h
#1: welcome to the world's helthiest food
usage: java [java-opts] -jar [jarfile]
 -h,--help           Show this help.
 -p,--port <arg>     Set the port the server will listen to (defaults to
                     8080).
```

For example:

```
$ java -jar target/basil-server-0.2.0-SNAPSHOT.jar -p 8080
#1: welcome to the world's helthiest food
ERROR StatusLogger No log4j2 configuration file found. Using default configuration: logging only errors to the console.
#2: basil is starting on port 8080
#3: done
#4: enjoy
```

You can specify a configuration for logging:

```
$ java -jar basil-server-0.2.0-SNAPSHOT.jar -Dlog4j.configurationFile=...
```

## Run with Maven ##

```
#!shell
$ cd basil
$ mvn jetty:run
```


## Quickstart

(Referenced files are in the codebase)

List APIs:

```
$ curl http://localhost:8080/basil
http://localhost:8080/basil/qxib7zacli5u
http://localhost:8080/basil/t4yfikuoe9tk
```

Create a new API:

```
$ curl -v http://localhost:8080/basil?endpoint=http://data.open.ac.uk/sparql -X PUT  -T src/test/resources/sparql/select_7.txt
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> PUT /basil?endpoint=http://data.open.ac.uk/sparql HTTP/1.1
> User-Agent: curl/7.41.0
> Host: localhost:8080
> Accept: */*
> Content-Length: 2720
> Expect: 100-continue
> 
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
< HTTP/1.1 201 Created
< Date: Mon, 01 Jun 2015 06:57:09 GMT
< Location: http://localhost:8080/basil/skk17pv4ajc4
< X-Basil-Api: http://localhost:8080/basil/skk17pv4ajc4/api
< X-Basil-Spec: http://localhost:8080/basil/skk17pv4ajc4/spec
< X-Basil-View: http://localhost:8080/basil/skk17pv4ajc4/view
< X-Basil-Docs: http://localhost:8080/basil/skk17pv4ajc4/docs
< X-Basil-Swagger: http://localhost:8080/basil/skk17pv4ajc4/api-docs
< Content-Type: text/plain
< Content-Length: 49
< Server: Jetty(9.2.z-SNAPSHOT)
< 
* Connection #0 to host localhost left intact
Created: http://localhost:8080/basil/skk17pv4ajc4
```

See created specification:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/spec
```

Access the Swagger description:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/api-docs
```
You can also access the HTML UI from a Web browser.

Query the api:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/api.json?qid=q18
```

or:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/api?qid=q18 -H "Accept: application/json"
http://localhost:8080/basil/skk17pv4ajc4/api.csv?qid=q18
http://localhost:8080/basil/skk17pv4ajc4/api.xml?qid=q18
http://localhost:8080/basil/skk17pv4ajc4/api.tsv?qid=q18
```

Create an html view, using a moustache template:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/view/html-list -X PUT -T src/test/resources/mustache/select_7.tmpl
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> PUT /basil/skk17pv4ajc4/view/html-list HTTP/1.1
> User-Agent: curl/7.41.0
> Host: localhost:8080
> Accept: */*
> Content-Length: 91
> Expect: 100-continue
> 
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
< HTTP/1.1 201 Created
< Date: Mon, 01 Jun 2015 07:04:58 GMT
< Location: http://localhost:8080/basil/skk17pv4ajc4/html-list
< Content-Length: 0
< Server: Jetty(9.2.z-SNAPSHOT)
< 
* Connection #0 to host localhost left intact
```

List the views:

```
$  curl http://localhost:8080/basil/skk17pv4ajc4/view
html-list

```

See the view script:

```
$  curl http://localhost:8080/basil/skk17pv4ajc4/view/html-list
<ol>
{{#items}}
<li>{{title}} ({{type}})<br/><small>{{link}}</small></li>
{{/items}}
</ol>
```

Get the data from the view:

```
$ curl -v http://localhost:8080/basil/skk17pv4ajc4/api.html-list?qid=q18
```

