package com.observatory.observatorysystem;

import com.observatory.observatorysystem.config.SessionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/current").hasAnyRole("SCIENTIST", "ADMIN")

                        // Телескопы - доступ для SCIENTIST и ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/telescopes", "/api/telescopes/{id}").hasAnyRole("SCIENTIST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/telescopes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/telescopes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/telescopes/**").hasRole("ADMIN")

                        // Программы - доступ для SCIENTIST и ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/programs", "/api/programs/{id}").hasAnyRole("SCIENTIST", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/programs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/programs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/programs/**").hasRole("ADMIN")

                        // Заявки
                        .requestMatchers("/api/observations/**").hasAnyRole("SCIENTIST", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .addFilterBefore(new SessionFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Бин для кодирования паролей - временно отключаем хеширование
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // ВНИМАНИЕ: только для разработки!
    }

    // Кастомный UserDetailsService для работы с БД
    @Bean
    public UserDetailsService userDetailsService(com.observatory.observatorysystem.repository.UserRepository userRepository) {
        return username -> {
            com.observatory.observatorysystem.entity.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            // Преобразуем  роли в формат Spring Security (добавляем ROLE_ префикс)
            String role = "ROLE_" + user.getRole();

            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                    .build();
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}