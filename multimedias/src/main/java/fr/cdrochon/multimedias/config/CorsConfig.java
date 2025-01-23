package fr.cdrochon.multimedias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost", "http://localhost:80", "http://localhost:4200", "http://localhost:8787", "http://localhost:4200",
//                                         "http://multimedias-server:8787/*", "http" +
//                "://multimedias-frontend:80", "http://multimedias-frontend"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//}

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:80", "http://localhost:8787", "http://localhost:4200", "http://multimedias-server:8787/*", "http" +
                        "://multimedias-frontend:80", "http://multimedias-frontend")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

