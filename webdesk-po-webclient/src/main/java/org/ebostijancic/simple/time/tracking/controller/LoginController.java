package org.ebostijancic.simple.time.tracking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController {

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login() {
		return "login";
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() {
		return "index";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signup() {
		return "signup";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)	
	public String register() {
		
		return "index";
	}
	
	@RequestMapping(value = "/user/{email}", method = RequestMethod.GET)
	public ResponseEntity<String> emailExists() {
		return new ResponseEntity<>(HttpStatus.FOUND);		
	}
}
