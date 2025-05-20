package com.springboot.MyTodoList.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public WebSecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher[] publicPaths = {
            new AntPathRequestMatcher("/usuarios/login"),
            new AntPathRequestMatcher("/usuarios/signup"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            // Static resources
            new AntPathRequestMatcher("/"),
            new AntPathRequestMatcher("/index.html"),
            new AntPathRequestMatcher("/static/**"),
            new AntPathRequestMatcher("/assets/**"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/manifest.json"),
            // SPA Routes - Allow React Router to handle these
            new AntPathRequestMatcher("/login"),
            new AntPathRequestMatcher("/signup"),
            new AntPathRequestMatcher("/home/**"),
            new AntPathRequestMatcher("/board/**"),
            new AntPathRequestMatcher("/sprint/**"),
            new AntPathRequestMatcher("/dashboard/**"),
            new AntPathRequestMatcher("/team/**"),
            new AntPathRequestMatcher("/test-auth/**")
        };

        http
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicPaths).permitAll()
                .anyRequest().authenticated())
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
