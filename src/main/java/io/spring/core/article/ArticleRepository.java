package io.spring.core.article;

import reactor.core.publisher.Mono;

public interface ArticleRepository {

  Mono<Article> save(Article article);

  Mono<Article> findById(String id);

  Mono<Article> findBySlug(String slug);

  Mono<Void> remove(Article article);
}
