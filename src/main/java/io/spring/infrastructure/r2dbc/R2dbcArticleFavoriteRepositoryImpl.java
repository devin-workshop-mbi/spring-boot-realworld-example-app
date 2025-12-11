package io.spring.infrastructure.r2dbc;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcArticleFavoriteRepositoryImpl implements ArticleFavoriteRepository {
  private final SpringDataArticleFavoriteRepository springDataArticleFavoriteRepository;

  @Override
  public Mono<Void> save(ArticleFavorite articleFavorite) {
    return springDataArticleFavoriteRepository.insertArticleFavorite(
        articleFavorite.getArticleId(), articleFavorite.getUserId());
  }

  @Override
  public Mono<ArticleFavorite> find(String articleId, String userId) {
    return springDataArticleFavoriteRepository.findByArticleIdAndUserId(articleId, userId);
  }

  @Override
  public Mono<Void> remove(ArticleFavorite favorite) {
    return springDataArticleFavoriteRepository.deleteByArticleIdAndUserId(
        favorite.getArticleId(), favorite.getUserId());
  }

  @Override
  public Mono<Long> count(String articleId) {
    return springDataArticleFavoriteRepository.countByArticleId(articleId);
  }
}
