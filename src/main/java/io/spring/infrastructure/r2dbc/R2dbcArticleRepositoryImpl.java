package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
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
public class R2dbcArticleRepositoryImpl implements ArticleRepository {
  private final SpringDataArticleRepository springDataArticleRepository;
  private final TagRepository tagRepository;

  @Override
  public Mono<Article> save(Article article) {
    return springDataArticleRepository.save(article)
        .flatMap(savedArticle -> {
          if (article.getTags() != null && !article.getTags().isEmpty()) {
            return tagRepository.deleteArticleTagsByArticleId(savedArticle.getId())
                .thenMany(
                    Flux.fromIterable(article.getTags())
                        .flatMap(tag -> 
                            tagRepository.findByName(tag.getName())
                                .switchIfEmpty(tagRepository.save(tag))
                                .flatMap(savedTag -> 
                                    tagRepository.insertArticleTag(savedArticle.getId(), savedTag.getId())
                                        .thenReturn(savedTag)
                                )
                        )
                )
                .then(Mono.just(savedArticle));
          }
          return Mono.just(savedArticle);
        });
  }

  @Override
  public Mono<Article> findById(String id) {
    return springDataArticleRepository.findById(id);
  }

  @Override
  public Mono<Article> findBySlug(String slug) {
    return springDataArticleRepository.findBySlug(slug);
  }

  @Override
  public Mono<Void> remove(Article article) {
    return tagRepository.deleteArticleTagsByArticleId(article.getId())
        .then(springDataArticleRepository.delete(article));
  }

  @Override
  public Flux<Article> findAll() {
    return springDataArticleRepository.findAll();
  }

  @Override
  public Mono<Long> count() {
    return springDataArticleRepository.count();
  }
}
