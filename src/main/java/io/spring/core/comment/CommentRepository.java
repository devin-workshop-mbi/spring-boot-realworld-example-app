package io.spring.core.comment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository {
  Mono<Comment> save(Comment comment);

  Mono<Comment> findById(String id);

  Mono<Comment> findById(String articleId, String id);

  Flux<Comment> findByArticleId(String articleId);

  Mono<Void> remove(Comment comment);
}
