package org.mvp.policy.embedder.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun addCorsMappings(reg: CorsRegistry) {
        reg.addMapping("/**")
            .allowedOriginPatterns("http://local-*.elandbo.co.kr:*", "https://*-*.elandbo.co.kr")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}