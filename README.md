 TODO
multi project plugin



### Getting started
To run this application:

````
	mvn exec:java
````

This launches a super simple ReSTful backend that uses [Spark](http://sparkjava.com/) to define routes.  Browse to localhost:4567/test.html

To watch changes to test.html, you can use a maven watch.

````
    mvn watcher:run
````

To send messages to kafka that will be consumed by this and pushed out to the browser, download kafka and build it.  Run

````
	./kafka-console-producer.sh --broker-list "bld-kafka8-01:9092" --topic pusher-test
    {"project": "project1", "message": "something changed from kafka"}
````

Any JSON that you write to the console will be sent along this topic.  There's a sample.json file you can also use.

````
     ./kafka-console-producer.sh --broker-list "bld-kafka8-01:9092" --topic pusher-test < /Users/smelody/projects/pusherBackend/sample.json
````

### Poxa
The on-prem version of Pusher is called Poxa (https://github.com/edgurgel/poxa).  We used this in Waffle and have a docker image available in quay.io/waffle/poxa.    To test with Poxa, change test.html's initialization of Pusher to use localhost and run PusherServer's main with "poxa" as the first argument.
I followed the instructions in the poxa readme and configure it pretty easily.  Browsing to Poxa's url gives you a debugging console.

### Things we've noticed:

 * Multiple channel subscriptions and event binding will all communicate over one web socket connection.  Messages come across as frames that have channel names in the payload.
 * Subscribing to multiple private channels can cause lots of authorization requests.  https://github.com/dirkbonhomme/pusher-js-auth is a solution for batching these requests.
 * You can configure the authentication/authorization endpoint in the pusher client library (/pusher/auth is the default)

### Things we need to decide on:

* Where does the authorization endpoint (currently called /pusher/auth in PusherServer.java) reside?  It seems like it should be in the same service as the service that processes change notifications and POSTs to pusher.
* Is the processing a separate service, pipeline, something else?  Or do we put it into pigeon?  Is Pusher's API really a webhook recipient?

