package de.rainu.restcommander.controller;

import de.rainu.restcommander.config.security.annotation.IsAdmin;
import de.rainu.restcommander.config.security.annotation.IsUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller contains the endpoints where you have to be logged in in and authorized.
 */
@RestController
public class HelloController {

	@IsAdmin
	@RequestMapping(path = "/hello/admin", method = RequestMethod.GET)
	public String sayHelloToTheAdmin() {
		return "Hello Admin!";
	}

	@IsUser
	@RequestMapping(path = "/hello", method = RequestMethod.GET)
	public String sayHello() {
		return "Hello World!";
	}

}
