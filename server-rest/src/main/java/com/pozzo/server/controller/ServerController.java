package com.pozzo.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/server-rest")
public class ServerController {
	@GetMapping(value = "/infos", produces = "application/json")
	public ResponseEntity<String> get() {
		String info = "Getting infos resource from Server";
		System.out.println(info);
		return ResponseEntity.ok(info);
	}
}
