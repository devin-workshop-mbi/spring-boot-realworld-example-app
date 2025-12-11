package io.spring.core.favorite;

import reactor.core.publisher.Mono;

public interface ArticleFavoriteRepository {
  Mono<Void> save(ArticleFavorite articleFavorite);

  Mono<ArticleFavorite> find(String articleId, String userId);

  Mono<Void> remove(ArticleFavorite favorite);

  Mono<Long> count(String articleId);
}
