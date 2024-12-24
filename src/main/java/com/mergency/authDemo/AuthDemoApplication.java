package com.mergency.authDemo;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.mergency.authDemo.config.DotEnvConfig;

@SpringBootApplication
public class AuthDemoApplication {

	public static void main(String[] args) {
		DotEnvConfig.loadEnv();
		SpringApplication.run(AuthDemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			try {
				String url = System.getProperty("DB_URL");
				String username = System.getProperty("DB_USERNAME");
				String password = System.getProperty("DB_PASSWORD");

				try (Connection connection = DriverManager.getConnection(url, username, password)) {
					if (connection != null) {
						System.out.println("^w^**** Connection Complete ****^w^");
					} else {
						throw new Exception("Connection Failed T^T");
					}
				}
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		};
	}

}
