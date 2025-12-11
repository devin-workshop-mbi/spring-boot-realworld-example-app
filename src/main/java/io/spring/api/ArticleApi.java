package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public Mono<Map<String, Object>> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .map(this::articleResponse);
  }

  @PutMapping
  public Mono<Map<String, Object>> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                return Mono.error(new NoAuthorizationException());
              }
              return articleCommandService
                  .updateArticle(article, updateArticleParam)
                  .flatMap(
                      updatedArticle ->
                          articleQueryService
                              .findBySlug(updatedArticle.getSlug(), user)
                              .map(this::articleResponse));
            });
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                return Mono.error(new NoAuthorizationException());
              }
              return articleRepository.remove(article);
            });
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    Map<String, Object> response = new HashMap<>();
    response.put("article", articleData);
    return response;
  }
}
