package com.cybr406.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

	@Value("${account.url}")
	String accountUrl;

	@Value("${post.url}")
	String postUrl;

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("example", r -> r
						.path("/")
						.uri("http://example.com"))
				.route("account", r -> r
						.path("/headers", "/signup", "/check-user", "/profiles/**")
						.uri(accountUrl))
				.route("post", r -> r
						.path("/posts/**", "/comments/**")
						.uri(postUrl))
				.build();
	}

}
