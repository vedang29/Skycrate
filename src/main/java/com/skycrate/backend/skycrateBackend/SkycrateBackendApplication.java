package com.skycrate.backend.skycrateBackend;

import com.skycrate.backend.skycrateBackend.controller.HDFScontroller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication
public class SkycrateBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkycrateBackendApplication.class, args);
	}
}
