package hexlet.code.config;

import hexlet.code.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@SuppressWarnings("java:S4502") // CSRF disabled for stateless JWT authentication
public class SecurityConfig {

    @Bean
    @SuppressWarnings("java:S4502") // CSRF disabled for stateless JWT authentication
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HandlerMappingIntrospector introspector,
            JwtDecoder jwtDecoder)
            throws Exception {

        // Если JWT не настроен (тестовый профиль) - отключаем security
        if (jwtDecoder == null) {
            return http
                    .csrf(AbstractHttpConfigurer::disable) // Safe for tests
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        // Основная конфигурация для production/dev
        return http
                .csrf(AbstractHttpConfigurer::disable) // Safe for stateless JWT authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/login", "/welcome").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((rs) -> rs.jwt((jwt) -> jwt.decoder(jwtDecoder)))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public AuthenticationProvider daoAuthProvider(
            CustomUserDetailsService userService,
            PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
