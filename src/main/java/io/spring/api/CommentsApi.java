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
import reactor.core.scheduler.Schedulers;

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
    return Mono.fromCallable(
            () -> {
              var article =
                  articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
              Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
              commentRepository.save(comment);
              return commentResponse(commentQueryService.findById(comment.getId(), user).get());
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping
  public Mono<Map<String, Object>> getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return Mono.fromCallable(
            () -> {
              var article =
                  articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
              var comments = commentQueryService.findByArticleId(article.getId(), user);
              Map<String, Object> response = new HashMap<>();
              response.put("comments", comments);
              return response;
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @DeleteMapping(path = "{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    return Mono.<Void>fromCallable(
            () -> {
              var article =
                  articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
              commentRepository
                  .findById(article.getId(), commentId)
                  .map(
                      comment -> {
                        if (!AuthorizationService.canWriteComment(user, article, comment)) {
                          throw new NoAuthorizationException();
                        }
                        commentRepository.remove(comment);
                        return null;
                      })
                  .orElseThrow(ResourceNotFoundException::new);
              return null;
            })
        .subscribeOn(Schedulers.boundedElastic());
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
