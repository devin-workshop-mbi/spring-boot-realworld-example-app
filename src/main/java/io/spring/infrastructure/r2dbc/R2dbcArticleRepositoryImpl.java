package io.spring.infrastructure.r2dbc;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Primary
@AllArgsConstructor
public class R2dbcArticleRepositoryImpl implements ArticleRepository {
  private final R2dbcArticleRepository r2dbcArticleRepository;
  private final R2dbcTagRepository r2dbcTagRepository;

  @Override
  public Mono<Article> save(Article article) {
    return r2dbcArticleRepository.save(article)
        .flatMap(savedArticle -> {
          if (article.getTags() != null && !article.getTags().isEmpty()) {
            return r2dbcTagRepository.deleteArticleTagsByArticleId(savedArticle.getId())
                .thenMany(
                    reactor.core.publisher.Flux.fromIterable(article.getTags())
                        .flatMap(tag -> 
                            r2dbcTagRepository.findByName(tag.getName())
                                .switchIfEmpty(r2dbcTagRepository.save(tag))
                                .flatMap(savedTag -> 
                                    r2dbcTagRepository.insertArticleTag(savedArticle.getId(), savedTag.getId())
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
    return r2dbcArticleRepository.findById(id);
  }

  @Override
  public Mono<Article> findBySlug(String slug) {
    return r2dbcArticleRepository.findBySlug(slug);
  }

  @Override
  public Mono<Void> remove(Article article) {
    return r2dbcTagRepository.deleteArticleTagsByArticleId(article.getId())
        .then(r2dbcArticleRepository.delete(article));
  }
}
