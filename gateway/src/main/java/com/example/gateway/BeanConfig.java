package com.example.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
class BeanConfig {
	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
			.route(r -> r.path("/users/**")
				.uri("lb://user"))
			.route(r -> r.path("/contests/**")
				.uri("lb://contest"))
			.route(r -> r.path("/votes/**")
				.uri("lb://vote"))
			.route(r -> r.path("/results/**")
				.uri("lb://result"))
			.build();
		}
}