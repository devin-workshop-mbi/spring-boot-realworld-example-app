package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcTagRepositoryImpl implements TagRepository {
  private final SpringDataTagRepository springDataTagRepository;

  @Override
  public Mono<Tag> findByName(String name) {
    return springDataTagRepository.findByName(name);
  }

  @Override
  public Flux<Tag> findByArticleId(String articleId) {
    return springDataTagRepository.findByArticleId(articleId);
  }

  @Override
  public Mono<Tag> save(Tag tag) {
    return springDataTagRepository.save(tag);
  }

  @Override
  public Mono<Void> insertArticleTag(String articleId, String tagId) {
    return springDataTagRepository.insertArticleTag(articleId, tagId);
  }

  @Override
  public Mono<Void> deleteArticleTagsByArticleId(String articleId) {
    return springDataTagRepository.deleteArticleTagsByArticleId(articleId);
  }

  @Override
  public Flux<String> findAllTagNames() {
    return springDataTagRepository.findAllTagNames();
  }
}
