package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Map<String, Object>> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              Comment comment =
                  new Comment(newCommentParam.getBody(), user.getId(), article.getId());
              return commentRepository
                  .save(comment)
                  .flatMap(
                      savedComment ->
                          commentQueryService
                              .findById(savedComment.getId(), user)
                              .map(this::commentResponse));
            });
  }

  @GetMapping
  public Mono<Map<String, Object>> getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article ->
                commentQueryService
                    .findByArticleId(article.getId(), user)
                    .collectList()
                    .map(
                        comments -> {
                          Map<String, Object> response = new HashMap<>();
                          response.put("comments", comments);
                          return response;
                        }));
  }

  @DeleteMapping(path = "{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
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
                          return commentRepository.remove(comment);
                        }));
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    Map<String, Object> response = new HashMap<>();
    response.put("comment", commentData);
    return response;
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
