package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Article;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcArticleRepository extends ReactiveCrudRepository<Article, String> {
  Mono<Article> findBySlug(String slug);
}
