package io.spring.infrastructure.r2dbc;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcCommentRepositoryImpl implements CommentRepository {
  private final SpringDataCommentRepository springDataCommentRepository;

  @Override
  public Mono<Comment> save(Comment comment) {
    return springDataCommentRepository.save(comment);
  }

  @Override
  public Mono<Comment> findById(String id) {
    return springDataCommentRepository.findById(id);
  }

  @Override
  public Mono<Comment> findById(String articleId, String id) {
    return springDataCommentRepository.findByArticleIdAndId(articleId, id);
  }

  @Override
  public Flux<Comment> findByArticleId(String articleId) {
    return springDataCommentRepository.findByArticleId(articleId);
  }

  @Override
  public Mono<Void> remove(Comment comment) {
    return springDataCommentRepository.delete(comment);
  }
}
