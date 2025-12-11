package io.spring.infrastructure.r2dbc;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcUserRepositoryImpl implements UserRepository {
  private final R2dbcUserRepository r2dbcUserRepository;
  private final R2dbcFollowRelationRepository r2dbcFollowRelationRepository;

  @Override
  public Mono<User> save(User user) {
    return r2dbcUserRepository.findById(user.getId())
        .flatMap(existing -> r2dbcUserRepository.save(user))
        .switchIfEmpty(r2dbcUserRepository.save(user));
  }

  @Override
  public Mono<User> findById(String id) {
    return r2dbcUserRepository.findById(id);
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return r2dbcUserRepository.findByUsername(username);
  }

  @Override
  public Mono<User> findByEmail(String email) {
    return r2dbcUserRepository.findByEmail(email);
  }

  @Override
  public Mono<Void> saveRelation(FollowRelation followRelation) {
    return r2dbcFollowRelationRepository
        .findByUserIdAndTargetId(followRelation.getUserId(), followRelation.getTargetId())
        .switchIfEmpty(
            r2dbcFollowRelationRepository.insertFollowRelation(
                followRelation.getUserId(), followRelation.getTargetId())
            .then(Mono.empty()))
        .then();
  }

  @Override
  public Mono<FollowRelation> findRelation(String userId, String targetId) {
    return r2dbcFollowRelationRepository.findByUserIdAndTargetId(userId, targetId);
  }

  @Override
  public Mono<Void> removeRelation(FollowRelation followRelation) {
    return r2dbcFollowRelationRepository.deleteByUserIdAndTargetId(
        followRelation.getUserId(), followRelation.getTargetId());
  }
}
