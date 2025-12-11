package io.spring.core.user;

import reactor.core.publisher.Mono;

public interface UserRepository {
  Mono<User> save(User user);

  Mono<User> findById(String id);

  Mono<User> findByUsername(String username);

  Mono<User> findByEmail(String email);

  Mono<Void> saveRelation(FollowRelation followRelation);

  Mono<FollowRelation> findRelation(String userId, String targetId);

  Mono<Void> removeRelation(FollowRelation followRelation);
}
