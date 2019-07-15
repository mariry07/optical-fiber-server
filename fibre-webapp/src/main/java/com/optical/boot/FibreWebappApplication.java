package com.optical.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class FibreWebappApplication {
	private static final Logger log = LoggerFactory.getLogger(FibreWebappApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(FibreWebappApplication.class, args);
		log.info("=======================here start FibreWebappApplication! ===========================");
	}



}
