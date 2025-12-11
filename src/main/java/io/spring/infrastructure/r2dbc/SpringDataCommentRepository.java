package io.spring.infrastructure.r2dbc;

import io.spring.core.comment.Comment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpringDataCommentRepository extends ReactiveCrudRepository<Comment, String> {
  @Query("SELECT * FROM comments WHERE article_id = :articleId AND id = :id")
  Mono<Comment> findByArticleIdAndId(String articleId, String id);

  Flux<Comment> findByArticleId(String articleId);
}
