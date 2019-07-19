package com.optical.boot;

import com.optical.Service.impl.OpticalServiceImpl;
import com.optical.bean.SocketProperties;
import com.optical.component.SocketListener;
import com.optical.component.SocketRunner;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import java.net.ServerSocket;

@SpringBootApplication
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
		"com.optical.bean",
		"com.optical.common",
		"com.optical.component",
		"com.optical.Service",
		"com.optical.Service.impl",
})
@MapperScan(basePackages = "com.optical.mapper")
public class FibreWebappApplication {
	private static final Logger log = LoggerFactory.getLogger(FibreWebappApplication.class);

	public static void main(String[] args) {

		//SpringApplication.run(FibreWebappApplication.class, args);

		ApplicationContext context = SpringApplication.run(FibreWebappApplication.class, args);
		log.info("=======================here start FibreWebappApplication! ===========================");
		context.getBean(SocketRunner.class).runrun();

	}


	@Primary
	@Bean
	public OpticalServiceImpl createOpticalServiceImpl() {
		OpticalServiceImpl opticalService = new OpticalServiceImpl();
		return opticalService;
	}

	@Primary
	@Bean
	public SocketProperties createSocketProperties() {
		SocketProperties socketProperties = new SocketProperties();
		return socketProperties;
	}

	@Bean
	public SocketRunner createSocketRunner() {
		SocketRunner socketRunner = new SocketRunner();
		return socketRunner;
	}


}
