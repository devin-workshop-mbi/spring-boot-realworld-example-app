package io.spring.infrastructure.r2dbc;

import io.spring.core.favorite.ArticleFavorite;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SpringDataArticleFavoriteRepository extends ReactiveCrudRepository<ArticleFavorite, Void> {
  @Query("SELECT * FROM article_favorites WHERE article_id = :articleId AND user_id = :userId")
  Mono<ArticleFavorite> findByArticleIdAndUserId(String articleId, String userId);

  @Modifying
  @Query("DELETE FROM article_favorites WHERE article_id = :articleId AND user_id = :userId")
  Mono<Void> deleteByArticleIdAndUserId(String articleId, String userId);

  @Modifying
  @Query("INSERT INTO article_favorites (article_id, user_id) VALUES (:articleId, :userId)")
  Mono<Void> insertArticleFavorite(String articleId, String userId);

  @Query("SELECT COUNT(*) FROM article_favorites WHERE article_id = :articleId")
  Mono<Long> countByArticleId(String articleId);
}
