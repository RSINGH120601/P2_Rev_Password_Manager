package com.rev.passwordmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class RevPasswordManagerApplication {

	private static final Logger log =
			LogManager.getLogger(RevPasswordManagerApplication.class);

	public static void main(String[] args) {

		log.info("Starting Password Manager Application...");

		SpringApplication.run(RevPasswordManagerApplication.class, args);

		log.info("Application Started Successfully");

	}
}