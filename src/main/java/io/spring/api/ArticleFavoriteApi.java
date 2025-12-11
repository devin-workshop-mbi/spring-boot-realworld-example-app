package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public Mono<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
              return articleFavoriteRepository
                  .save(articleFavorite)
                  .then(articleQueryService.findBySlug(slug, user).map(this::responseArticleData));
            });
  }

  @DeleteMapping
  public Mono<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article ->
                articleFavoriteRepository
                    .find(article.getId(), user.getId())
                    .flatMap(articleFavoriteRepository::remove)
                    .then(articleQueryService.findBySlug(slug, user).map(this::responseArticleData)));
  }

  private Map<String, Object> responseArticleData(final ArticleData articleData) {
    Map<String, Object> response = new HashMap<>();
    response.put("article", articleData);
    return response;
  }
}
