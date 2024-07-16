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
