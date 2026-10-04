package com.proyectoApi.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoApi.api.rmi.RmiGateway;
import com.proyectoApi.api.service.MenuMetadata;
import com.proyectoApi.api.service.MenuService;
import com.proyectoApi.api.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Servicios de aplicación (Java puro) registrados como beans. */
@Configuration
public class ServiceConfig {

    @Bean
    public MenuMetadata menuMetadata(ResourceLoader loader, ObjectMapper mapper,
                                     @Value("${app.menu-config}") String location) throws IOException {
        Resource resource = loader.getResource(location);
        InputStream in = resource.exists() ? resource.getInputStream() : null;
        return MenuMetadata.loadOrEmpty(in, mapper, location);
    }

    @Bean
    public MenuService menuService(RmiGateway rmi, MenuMetadata metadata,
                                   @Value("${app.menu.cache-seconds:15}") int cacheSeconds) {
        return new MenuService(rmi, metadata, cacheSeconds);
    }

    @Bean
    public OrderService orderService(RmiGateway rmi,
                                     @Value("${app.orders.payment-methods}") String[] paymentMethods) {
        return new OrderService(rmi, Arrays.stream(paymentMethods).map(String::trim).filter(s -> !s.isEmpty()).toList());
    }
}
