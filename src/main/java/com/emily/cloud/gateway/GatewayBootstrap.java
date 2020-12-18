package com.emily.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(GatewayBootstrap.class, args);
    }

/*    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes().route(p->p.path("/get").uri("https://wwww.baidu.com/")).build();
    }*/
}