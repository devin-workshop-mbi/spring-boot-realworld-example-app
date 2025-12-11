package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Article;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SpringDataArticleRepository extends ReactiveCrudRepository<Article, String> {
  Mono<Article> findBySlug(String slug);
}
