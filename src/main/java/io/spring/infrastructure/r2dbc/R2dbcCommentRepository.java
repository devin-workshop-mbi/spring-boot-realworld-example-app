package io.spring.infrastructure.r2dbc;

import io.spring.core.comment.Comment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcCommentRepository extends ReactiveCrudRepository<Comment, String> {
  @Query("SELECT * FROM comments WHERE article_id = :articleId AND id = :id")
  Mono<Comment> findByArticleIdAndId(String articleId, String id);

  Flux<Comment> findByArticleId(String articleId);
}
