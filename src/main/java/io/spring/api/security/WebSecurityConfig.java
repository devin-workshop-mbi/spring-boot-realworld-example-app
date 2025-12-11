package io.spring.api.security;

import static java.util.Arrays.asList;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      UserRepository userRepository,
      JwtService jwtService) {
    return http.csrf()
        .disable()
        .cors()
        .configurationSource(corsConfigurationSource())
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(
            (exchange, ex) -> {
              exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
              return Mono.empty();
            })
        .and()
        .authorizeExchange()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .pathMatchers("/graphiql")
        .permitAll()
        .pathMatchers("/graphql")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/articles/feed")
        .authenticated()
        .pathMatchers(HttpMethod.POST, "/users", "/users/login")
        .permitAll()
        .pathMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/tags")
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .addFilterAt(new JwtTokenFilter(userRepository, jwtService), SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(asList("*"));
    configuration.setAllowedMethods(asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
    configuration.setAllowCredentials(false);
    configuration.setAllowedHeaders(asList("Authorization", "Cache-Control", "Content-Type"));
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
