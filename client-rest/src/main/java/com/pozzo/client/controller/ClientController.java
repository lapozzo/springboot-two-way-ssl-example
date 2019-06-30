package com.pozzo.client.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/client-rest")
public class ClientController {
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Environment env;

	@GetMapping(value = "/infos", produces = "application/json")
	public ResponseEntity<String> getMsData() {
		try {
			String msEndpoint = env.getProperty("endpoint.ms-service");
			System.out.println("Calling server url " + msEndpoint);

			String serverResponse = restTemplate.getForObject(new URI(msEndpoint), String.class);
			return ResponseEntity.ok("From Client: " + serverResponse);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
