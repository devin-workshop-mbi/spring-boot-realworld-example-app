package io.spring.infrastructure.r2dbc;

import io.spring.core.user.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SpringDataUserRepository extends ReactiveCrudRepository<User, String> {
  Mono<User> findByUsername(String username);

  Mono<User> findByEmail(String email);
}
