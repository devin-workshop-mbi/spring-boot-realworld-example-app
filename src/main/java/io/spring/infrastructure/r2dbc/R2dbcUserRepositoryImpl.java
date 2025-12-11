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
  private final SpringDataUserRepository springDataUserRepository;
  private final SpringDataFollowRelationRepository springDataFollowRelationRepository;

  @Override
  public Mono<User> save(User user) {
    return springDataUserRepository.findById(user.getId())
        .flatMap(existing -> springDataUserRepository.save(user))
        .switchIfEmpty(springDataUserRepository.save(user));
  }

  @Override
  public Mono<User> findById(String id) {
    return springDataUserRepository.findById(id);
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return springDataUserRepository.findByUsername(username);
  }

  @Override
  public Mono<User> findByEmail(String email) {
    return springDataUserRepository.findByEmail(email);
  }

  @Override
  public Mono<Void> saveRelation(FollowRelation followRelation) {
    return springDataFollowRelationRepository
        .findByUserIdAndTargetId(followRelation.getUserId(), followRelation.getTargetId())
        .switchIfEmpty(
            springDataFollowRelationRepository.insertFollowRelation(
                followRelation.getUserId(), followRelation.getTargetId())
            .then(Mono.empty()))
        .then();
  }

  @Override
  public Mono<FollowRelation> findRelation(String userId, String targetId) {
    return springDataFollowRelationRepository.findByUserIdAndTargetId(userId, targetId);
  }

  @Override
  public Mono<Void> removeRelation(FollowRelation followRelation) {
    return springDataFollowRelationRepository.deleteByUserIdAndTargetId(
        followRelation.getUserId(), followRelation.getTargetId());
  }
}
