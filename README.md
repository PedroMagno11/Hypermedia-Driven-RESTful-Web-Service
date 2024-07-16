# Hypermedia-Driven-RESTful-Web-Service

A “Hello, World” Hypermedia-driven REST web service with Spring.

Hypermedia is an important aspect of REST. It lets you build services that decouple client and server to a large extent and let them evolve independently. The representations returned for REST resources contain not only data but also links to related resources. Thus, the design of the representations is crucial to the design of the overall service.

## What you will build

You will build a hypermedia-driven REST service with Spring HATEOAS: a library of APIs that you can use to create links that point to Spring MVC controllers, build up resource representations, and control how they are rendered into supported hypermedia formats (such as HAL).

The service will accept HTTP GET requests at http://localhost:8080/greeting.

It will respond with a JSON representation of a greeting that is enriched with the simplest possible hypermedia element, a link that points to the resource itself. The following listing shows the output:

````json
{
  "content": "Hello, World!",
  "_links": {
    "self": {
      "href": "http://localhost:8080/greeting?name=World"
    }
  }
}
````

The response already indicates that you can customize the greeting with 
an optional `name` parameter in the query string, as the following listing 
shows:

```
http://localhost:8080/greeting?name=User
```

The `name` parameter value overrides the default value of `World` and is reflected in the response, as the following listing shows:

````json
{
  "content": "Hello, User!",
  "_links": {
    "self": {
      "href": "http://localhost:8080/greeting?name=User"
    }
  }
}
````

## What you need

- A favorite text editor or IDE
- Java 17 or later
- Gradle 7.5+ or Maven 3.5+
- You can also import the code straight into your IDE:
  - Spring Tool Suite (STS)
  - IntelliJ IDEA
  - VSCode

## Starting with Spring Initializr

1 - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.

2 - Choose either Gradle or Maven and the language you want to use. This guide assumes that you chose Java.

3 - Click Dependencies and select Spring HATEOAS.

4 - Click Generate.

5 - Download the resulting ZIP file, which is an archive of a web application that is configured with your choices.

``If your IDE has the Spring Initializr integration, you can complete this process from your IDE.``

``You can also fork the project from Github and open it in your IDE or other editor.``

## Create a resource Representation Class

Now that you have set up the project and build system, you can create your web service.

Begin the process by thinking about service interactions.

The service will expose a resource at `/greeting` to handle `GET` requests, optionally with a `name` parameter in the query string. The `GET` request should return a `200 OK` response with JSON in the body to represent a greeting.

Beyond that, the JSON representation of the resource will be enriched with a list of hypermedia elements in a `_links` property. The most rudimentary form of this is a link that points to the resource itself. The representation should resemble the following listing:

````json
{
  "content": "Hello, World!",
  "_link": {
    "self": {
      "href": "http://localhost:8080/greeting?name=World"
    }
  }
}
````

The `content` is the textual representation of the 
greeting. The `_links` element contains a list 
of links (in this case, exactly one with 
the relation type of `rel` and the `href` attribute pointing to the resource that was accessed).

To model the greeting representation, 
create a resource representation 
class. As the `_links` property is 
a fundamental property of the 
representation model, Spring 
HATEOAS ships with a base class 
(called `RepresentationModel`) that 
lets you add instances of `Link` 
and ensures that they are 
rendered as shown earlier.

Create a plain old java object that 
extends `RepresentationModel` and adds the field and accessor for the content as well as a constructor, as the following listing 
(from src/main/java/br/com/pedromagno/resthateoas/domain/Greeting.java) shows:

````java
package br.com.pedromagno.resthateoas.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

public class Greeting extends RepresentationModel<Greeting> {
    private final String content;

    @JsonCreator
    public Greeting(@JsonProperty("content") String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }
}
````

- `@JsonCreator`: Signals how Jackson can create an instance of this POJO.
- `@JsonProperty`: Marks the field into which Jackson should put this constructor argument.


OBS:
`As you will see in later in this guide, Spring will use the Jackson JSON library to automatically marshal instances of type Greeting into JSON`

Next, create the resource controller that will serve these greetings.

## Create a REST Controller

In Spring’s approach to building 
RESTful web services, HTTP 
requests are handled by a 
controller. The components 
are identified by the `@RestController` 
annotation, which combines 
the `@Controller` and `@ResponseBody` 
annotations. The following 
`GreetingController` 
(from `src/main/java/br/com/pedromagno/resthateoas/controller/GreetingController.java`) 
handles `GET` requests for `/greeting` 
by returning a new instance 
of the `Greeting` class:

````java
package br.com.pedromagno.resthateoas.controller;

import br.com.pedromagno.resthateoas.domain.Greeting;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class GreetingController {
    private static final String TEMPLATE = "Hello, %s!";

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public HttpEntity<Greeting> greeting(@RequestParam(value = "name", defaultValue = "World") String name){
        Greeting greeting = new Greeting(String.format(TEMPLATE, name));
        greeting.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
        return new ResponseEntity<Greeting>(greeting, HttpStatus.OK);
    }
}
````

This controller is concise and simple, but there is plenty going on. We break it down step by step.

The `@RequestMapping` annotation ensures that HTTP requests to `/greeting` are mapped to the `greeting()` method.

OBS: ``The above example does not specify GET vs. PUT, POST, and so forth, because @RequestMapping maps all HTTP operations by default. Use @GetMapping("/greeting") to narrow this mapping. In that case you also want to import org.springframework.web.bind.annotation.GetMapping;``.

`@RequestParam` binds the value of 
the query string parameter `name` 
into the `name` parameter of the 
`greeting()` method. This query 
string parameter is implicitly 
not required because of 
the use of the `defaultValue` attribute. 
If it is absent in the 
request, the `defaultValue` of 
`World` is used.

Because the `@RestController` 
annotation is present on the 
class, an implicit 
`@ResponseBody` annotation 
is added to the `greeting` 
method. This causes Spring 
MVC to render the 
returned `HttpEntity` and 
its payload (the `Greeting`) 
directly to the response.

The most interesting part of the method 
implementation is how you create the 
link that points to the 
controller method and how 
you add it to the representation 
model. Both `linkTo(…)` and `methodOn(…)` 
are static methods on `ControllerLinkBuilder` 
that let you fake a method 
invocation on the controller. 
The returned `LinkBuilder` will 
have inspected the controller method’s mapping annotation to build up exactly the URI to which the method is mapped.

Spring HATEOAS respects various `X-FORWARDED-` headers. 
If you put a Spring HATEOAS service behind a proxy and 
properly configure it with `X-FORWARDED-HOST` 
headers, the resulting links will be properly 
formatted.

The call to `withSelfRel()` 
creates a `Link` instance that 
you add to the `Greeting` 
representation model.

`@SpringBootApplication` is a convenience annotation that adds all of the following:

- `@Configuration`: Tags the class as a source of bean definitions for the application context.

- `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on 
classpath settings, other beans, and various 
property settings. For example, if `spring-webmvc` is on the 
classpath, this annotation flags the application 
as a web application and activates 
key behaviors, such as setting up a `DispatcherServlet`.

- `@ComponentScan`: Tells Spring to look for 
other components, configurations, and services in the br/com/pedromagno package, letting it find the controllers.

The `main()` method uses Spring Boot’s 
`SpringApplication.run()` method to launch 
an application. Did you notice that 
there was not a single line of XML? 
There is no `web.xml` file, either. This web application is 100% pure Java and you did not have to deal with configuring any plumbing or infrastructure.

## Test the service

With the service running, visit http://localhost:8080/greeting, where you should see the following content:

````json
{
  "content":"Hello, World!",
  "_links":{
    "self":{
      "href":"http://localhost:8080/greeting?name=World"
    }
  }
}
````

Provide a `name` query string parameter by visiting the following URL: 
[http://localhost:8080/greeting?name=User](http://localhost:8080/greeting?name=User). 
Notice how the value of the `content` attribute changes 
from `Hello, World!` to `Hello, User!` and the `href` 
attribute of the `self` link 
reflects that change as well, as the following listing shows:

````json
{
  "content":"Hello, User!",
  "_links":{
    "self":{
      "href":"http://localhost:8080/greeting?name=User"
    }
  }
}
````
This change demonstrates 
that the `@RequestParam` 
arrangement in `GreetingController` 
works as expected. The `name` parameter has been 
given a default value of `World` but can always be explicitly overridden through the query string.