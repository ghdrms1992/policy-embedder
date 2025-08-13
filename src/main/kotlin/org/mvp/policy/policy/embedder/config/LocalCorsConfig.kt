package org.mvp.policy.policy.embedder.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("local")
class LocalCorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(reg: CorsRegistry) {
        reg.addMapping("/**")
            .allowedOriginPatterns("http://local-*.elandbo.co.kr:*")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600)
    }
}