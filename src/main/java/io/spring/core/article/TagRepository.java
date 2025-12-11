package io.spring.core.article;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TagRepository {

  Mono<Tag> findByName(String name);

  Flux<Tag> findByArticleId(String articleId);

  Mono<Tag> save(Tag tag);

  Mono<Void> insertArticleTag(String articleId, String tagId);

  Mono<Void> deleteArticleTagsByArticleId(String articleId);

  Flux<String> findAllTagNames();
}
