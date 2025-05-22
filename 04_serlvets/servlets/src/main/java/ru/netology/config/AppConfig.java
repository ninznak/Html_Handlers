package ru.netology.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.netology.controller.PostController;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {PostController.class})
public class AppConfig {
}