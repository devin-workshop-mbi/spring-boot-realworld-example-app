package io.spring.core.comment;

import reactor.core.publisher.Mono;

public interface CommentRepository {
  Mono<Comment> save(Comment comment);

  Mono<Comment> findById(String articleId, String id);

  Mono<Void> remove(Comment comment);
}
