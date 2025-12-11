package io.spring.infrastructure.r2dbc;

import io.spring.core.user.FollowRelation;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcFollowRelationRepository extends ReactiveCrudRepository<FollowRelation, Void> {
  @Query("SELECT * FROM follows WHERE user_id = :userId AND follow_id = :targetId")
  Mono<FollowRelation> findByUserIdAndTargetId(String userId, String targetId);

  @Modifying
  @Query("DELETE FROM follows WHERE user_id = :userId AND follow_id = :targetId")
  Mono<Void> deleteByUserIdAndTargetId(String userId, String targetId);

  @Modifying
  @Query("INSERT INTO follows (user_id, follow_id) VALUES (:userId, :targetId)")
  Mono<Void> insertFollowRelation(String userId, String targetId);
}
