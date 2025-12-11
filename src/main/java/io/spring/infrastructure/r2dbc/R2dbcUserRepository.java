package io.spring.infrastructure.r2dbc;

import io.spring.core.user.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcUserRepository extends ReactiveCrudRepository<User, String> {
  Mono<User> findByUsername(String username);

  Mono<User> findByEmail(String email);
}
