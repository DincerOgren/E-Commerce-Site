package org.example.project.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}

//spring.datasource.url = jdbc:postgresql://ecommerce.c7qeugymk14b.eu-north-1.rds.amazonaws.com:5432/myDatabase
//
//spring.datasource.username=postgres
//spring.datasource.password=dino123.
//
//        spring.jpa.hibernate.ddl-auto=update
//spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect