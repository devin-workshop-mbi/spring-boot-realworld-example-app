package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public CompletableFuture<DataFetcherResult<CommentPayload>> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              Comment comment = new Comment(body, user.getId(), article.getId());
              return commentRepository
                  .save(comment)
                  .flatMap(
                      savedComment ->
                          commentQueryService
                              .findById(savedComment.getId(), user)
                              .switchIfEmpty(Mono.error(new ResourceNotFoundException())));
            })
        .map(
            commentData ->
                DataFetcherResult.<CommentPayload>newResult()
                    .localContext(commentData)
                    .data(CommentPayload.newBuilder().build())
                    .build())
        .toFuture();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteComment)
  public CompletableFuture<DeletionStatus> removeComment(
      @InputArgument("slug") String slug, @InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article ->
                commentRepository
                    .findById(article.getId(), commentId)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                    .flatMap(
                        comment -> {
                          if (!AuthorizationService.canWriteComment(user, article, comment)) {
                            return Mono.error(new NoAuthorizationException());
                          }
                          return commentRepository
                              .remove(comment)
                              .thenReturn(DeletionStatus.newBuilder().success(true).build());
                        }))
        .toFuture();
  }
}
