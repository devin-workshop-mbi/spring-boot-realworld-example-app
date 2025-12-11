package io.spring.infrastructure.r2dbc;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcCommentRepositoryImpl implements CommentRepository {
  private final R2dbcCommentRepository r2dbcCommentRepository;

  @Override
  public Mono<Comment> save(Comment comment) {
    return r2dbcCommentRepository.save(comment);
  }

  @Override
  public Mono<Comment> findById(String articleId, String id) {
    return r2dbcCommentRepository.findByArticleIdAndId(articleId, id);
  }

  @Override
  public Mono<Void> remove(Comment comment) {
    return r2dbcCommentRepository.delete(comment);
  }
}
