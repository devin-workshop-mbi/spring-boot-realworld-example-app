package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtTokenFilter implements WebFilter {
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private static final String HEADER = "Authorization";

  public JwtTokenFilter(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HEADER);
    Optional<String> tokenOpt = getTokenString(authHeader);

    if (tokenOpt.isEmpty()) {
      return chain.filter(exchange);
    }

    Optional<String> userIdOpt = tokenOpt.flatMap(jwtService::getSubFromToken);

    if (userIdOpt.isEmpty()) {
      return chain.filter(exchange);
    }

    return userRepository
        .findById(userIdOpt.get())
        .flatMap(
            user -> {
              UsernamePasswordAuthenticationToken authenticationToken =
                  new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
              return chain
                  .filter(exchange)
                  .contextWrite(
                      ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
            })
        .switchIfEmpty(chain.filter(exchange));
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
