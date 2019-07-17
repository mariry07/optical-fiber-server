package com.optical.boot;

import com.optical.component.SocketRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
		"com.optical.bean",
		"com.optical.common",
		"com.optical.component",
		"com.optical.Service",
		"com.optical.Service.impl",
})
public class FibreWebappApplication {
	private static final Logger log = LoggerFactory.getLogger(FibreWebappApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(FibreWebappApplication.class, args);
		log.info("=======================here start FibreWebappApplication! ===========================");

	}

	@Bean
	public SocketRunner schedulerRunner() {
		return new SocketRunner();
	}

}
