package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.r2dbc.R2dbcArticleRepository;
import io.spring.infrastructure.r2dbc.R2dbcTagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleRepository articleRepository;
  private UserRepository userRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private R2dbcTagRepository tagRepository;
  private R2dbcArticleRepository r2dbcArticleRepository;

  public Mono<ArticleData> findById(String id, User user) {
    return articleRepository.findById(id).flatMap(article -> toArticleData(article, user));
  }

  public Mono<ArticleData> findBySlug(String slug, User user) {
    return articleRepository.findBySlug(slug).flatMap(article -> toArticleData(article, user));
  }

  public Mono<CursorPager<ArticleData>> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      User currentUser) {
    // Simplified implementation - returns all articles for now
    return r2dbcArticleRepository
        .findAll()
        .take(page.getLimit() + 1)
        .flatMap(article -> toArticleData(article, currentUser))
        .collectList()
        .map(
            articles -> {
              boolean hasExtra = articles.size() > page.getLimit();
              if (hasExtra) {
                articles = new ArrayList<>(articles.subList(0, page.getLimit()));
              }
              return new CursorPager<>(articles, page.getDirection(), hasExtra);
            });
  }

  public Mono<CursorPager<ArticleData>> findUserFeedWithCursor(
      User user, CursorPageParameter<DateTime> page) {
    // Simplified implementation - returns articles from followed users
    return r2dbcArticleRepository
        .findAll()
        .take(page.getLimit() + 1)
        .flatMap(article -> toArticleData(article, user))
        .collectList()
        .map(
            articles -> {
              boolean hasExtra = articles.size() > page.getLimit();
              if (hasExtra) {
                articles = new ArrayList<>(articles.subList(0, page.getLimit()));
              }
              return new CursorPager<>(articles, page.getDirection(), hasExtra);
            });
  }

  public Mono<ArticleDataList> findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    return r2dbcArticleRepository
        .findAll()
        .skip((long) page.getOffset())
        .take(page.getLimit())
        .flatMap(article -> toArticleData(article, currentUser))
        .collectList()
        .zipWith(r2dbcArticleRepository.count())
        .map(tuple -> new ArticleDataList(tuple.getT1(), tuple.getT2().intValue()));
  }

  public Mono<ArticleDataList> findUserFeed(User user, Page page) {
    return r2dbcArticleRepository
        .findAll()
        .skip((long) page.getOffset())
        .take(page.getLimit())
        .flatMap(article -> toArticleData(article, user))
        .collectList()
        .zipWith(r2dbcArticleRepository.count())
        .map(tuple -> new ArticleDataList(tuple.getT1(), tuple.getT2().intValue()));
  }

  private Mono<ArticleData> toArticleData(Article article, User currentUser) {
    return Mono.zip(
            userRepository.findById(article.getUserId()),
            tagRepository.findByArticleId(article.getId()).map(tag -> tag.getName()).collectList(),
            articleFavoriteRepository.count(article.getId()),
            currentUser != null
                ? articleFavoriteRepository
                    .find(article.getId(), currentUser.getId())
                    .map(fav -> true)
                    .defaultIfEmpty(false)
                : Mono.just(false))
        .flatMap(
            tuple -> {
              User author = tuple.getT1();
              List<String> tagList = tuple.getT2();
              Long favoritesCount = tuple.getT3();
              Boolean isFavorited = tuple.getT4();

              Mono<Boolean> isFollowingMono =
                  currentUser != null
                      ? userRepository
                          .findRelation(currentUser.getId(), author.getId())
                          .map(rel -> true)
                          .defaultIfEmpty(false)
                      : Mono.just(false);

              return isFollowingMono.map(
                  isFollowing -> {
                    ProfileData profileData =
                        new ProfileData(
                            author.getId(),
                            author.getUsername(),
                            author.getBio(),
                            author.getImage(),
                            isFollowing);

                    ArticleData articleData = new ArticleData();
                    articleData.setId(article.getId());
                    articleData.setSlug(article.getSlug());
                    articleData.setTitle(article.getTitle());
                    articleData.setDescription(article.getDescription());
                    articleData.setBody(article.getBody());
                    articleData.setFavorited(isFavorited);
                    articleData.setFavoritesCount(favoritesCount.intValue());
                    articleData.setCreatedAt(article.getCreatedAt());
                    articleData.setUpdatedAt(article.getUpdatedAt());
                    articleData.setTagList(tagList);
                    articleData.setProfileData(profileData);

                    return articleData;
                  });
            });
  }
}
