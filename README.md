JBus
=======

[![Javadocs](https://www.javadoc.io/badge/org.dizitart/jbus.svg)](https://www.javadoc.io/doc/org.dizitart/jbus) 
[![build status](https://gitlab.com/dizitart/jbus/badges/master/build.svg)](https://gitlab.com/dizitart/jbus/commits/master)
[![codecov](https://codecov.io/gl/dizitart/jbus/branch/master/graph/badge.svg)](https://codecov.io/gl/dizitart/jbus)

An event bus for java 1.6+. It dispatches event to registered listeners.

It is a simple but powerful publish-subscribe event system. It requires object to
register themselves with the event bus to receive events. This event bus is safe for
concurrent use.

It has the following features:

 * Light-weight but powerful
 * Annotation based API
 * Supports synchronous/asynchronous invocation of subscriber method
 * Handler chain interruption
 * Supports custom error handling
 * Supports strong/weak references of subscriber
 * Supports subscriber inheritance
 * Zero configuration
 * Optional JVM shutdown hook for graceful shutdown
 
Example
-------------------

<h5>Define events:</h5>

```java

    public class UserEvent { 
        /* Additional fields if required */ 
    }
    
```

<h5>Create subscriber:</h5>

```java

    public class Listener {
        
        @Subscribe
        private void listen(UserEvent event) {
            /* Your event handling code goes here */
        }
    }

```
 
<h5>Register subscriber:</h5>
    
```java
    
     JBus jbus = new JBus();
     jbus.register(new Listener());   
     // or to register with weak reference use jbus.registerWeak(new Listener());
``` 

<h5>Post event:</h5>

```java

    jbus.post(new UserEvent());

```

It is that simple.

Installation
-----------------------------

JBus is available from the Maven Central Repository using the following coordinates:

```xml
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>jbus</artifactId>
        <version>{latest.tag.release}</version>
    </dependency>
```


Details
-----------------------------

To receive events from event bus, an object must:

 * register itself with the event bus via <code>jbus.register(Object)</code> or
 <code>jbus.registerWeak(Object)</code> method
 * have at least one method marked with <code>@Subscribe</code> annotation in its class hierarchy
 * subscribed method should accept <b>only one parameter</b> having the type of the event
 
Registration will scan the input object for any method which has been marked
with <code>@Subscribe</code> annotation and it will keep track of all such methods found. If the
subscribing method has more than one parameter, runtime will throw a <code>JBusException</code>
during registration. A subscriber method can have any access modifier. Registration will scan through
full class hierarchy of the input object including any super class and interfaces.

Upon successful registration, the runtime will keep track of all subscriber methods
found along with a strong reference of the input object for future invocation. To store
a weak reference of the input object instead of a strong one, use the <code>jbus.registerWeak(Object)</code>
variant.

A developer must use <code>jbus.deregister(Object)</code> for the object to stop receiving events. The
behavior of de-registration is not deterministic in the case of <b><em>weak
registration</em></b> of the object. As the runtime automatically cleans up any invalid weak
references and any subscriber methods associated with it as it goes, so a call to
<code>jbus.deregister(Object)</code> might not do anything if the object has already been garbage collected and 
the event bus runtime has cleared up its records of subscriber methods already.

To post an event to the event bus, simply call jbus.post(Object) passing the event object. Event bus will
automatically route the event depending on its type to a handler chain. Handler chain is a collection of 
registered subscribers of the event. By design, event bus does not support inheritance for the event object.

If an event implements <code>HandlerChainAware</code> interface then before each invocation, the runtime
will check if an interruption has been signalled from the subscriber code via <code>HandlerChain.interrupt()</code>
call. If interrupted, further invocation of the handler chain will be barred until the next 
<code>jbus.post(Object)</code> call for the event.

Subscriber execution mode can be either <em>synchronous</em> or <em>asynchronous</em>
depending on the <code>@Subscribe</code> annotation declaration.

In case of any error from subscriber code during invocation, the runtime will first search
for any <code>ExceptionEvent</code> handler registered into the system and dispatch the error along with
relevant information in <code>ExceptionContext</code> to the handler if found. If no such error handler
is found, runtime will just log the error and move on.


Contribute
--------------------------
Please feel free to contribute by creating a pull request at [Gitlab page](https://gitlab.com/dizitart/jbus).
Please make sure your pull request is small and provided with test code.


License
--------------------------
This project is distributed under the terms of the Apache License, Version 2.0.
See file "LICENSE" for further reference.
