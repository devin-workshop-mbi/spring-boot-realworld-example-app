package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Tag;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpringDataTagRepository extends ReactiveCrudRepository<Tag, String> {
  Mono<Tag> findByName(String name);

  @Query("SELECT t.* FROM tags t INNER JOIN article_tags at ON t.id = at.tag_id WHERE at.article_id = :articleId")
  Flux<Tag> findByArticleId(String articleId);

  @Modifying
  @Query("INSERT INTO article_tags (article_id, tag_id) VALUES (:articleId, :tagId)")
  Mono<Void> insertArticleTag(String articleId, String tagId);

  @Modifying
  @Query("DELETE FROM article_tags WHERE article_id = :articleId")
  Mono<Void> deleteArticleTagsByArticleId(String articleId);

  @Query("SELECT DISTINCT t.name FROM tags t")
  Flux<String> findAllTagNames();
}
