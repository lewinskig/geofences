package com.lewinskig.geofences.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class StaticResourceConfig : WebMvcConfigurer{

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/frontend/**")
            .addResourceLocations("file:./frontend/")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/")
            .setViewName("forward:/frontend/index.html")
    }
}