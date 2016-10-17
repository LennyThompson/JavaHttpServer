# JavaHttpServer
Simple java application that serves the host file system to a browser.

## Usage

Start the server, say locally, with a command line argument of the port the server should serve on (for instance):

```
$ java -jar target/JavaHttpServer-1.0-SNAPSHOT.jar PORT=5555
```

This should echo the port and current runtime directory (for instance).

```
$ Enter any charcter tp  terminate:
$ Starting new HttpServer on port: 5555 at root: /home/lenny/git/JavaHttpServer
```

Then browse to *localhost:12345*, which will show something like:



