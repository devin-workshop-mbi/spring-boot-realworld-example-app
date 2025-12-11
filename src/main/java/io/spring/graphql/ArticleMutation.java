package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@DgsComponent
@AllArgsConstructor
public class ArticleMutation {

  private ArticleCommandService articleCommandService;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;

  @DgsMutation(field = MUTATION.CreateArticle)
  public CompletableFuture<DataFetcherResult<ArticlePayload>> createArticle(
      @InputArgument("input") CreateArticleInput input) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .body(input.getBody())
            .tagList(input.getTagList() == null ? Collections.emptyList() : input.getTagList())
            .build();
    return articleCommandService
        .createArticle(newArticleParam, user)
        .map(
            article ->
                DataFetcherResult.<ArticlePayload>newResult()
                    .data(ArticlePayload.newBuilder().build())
                    .localContext(article)
                    .build())
        .toFuture();
  }

  @DgsMutation(field = MUTATION.UpdateArticle)
  public CompletableFuture<DataFetcherResult<ArticlePayload>> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput params) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                return Mono.error(new NoAuthorizationException());
              }
              return articleCommandService.updateArticle(
                  article,
                  new UpdateArticleParam(params.getTitle(), params.getBody(), params.getDescription()));
            })
        .map(
            article ->
                DataFetcherResult.<ArticlePayload>newResult()
                    .data(ArticlePayload.newBuilder().build())
                    .localContext(article)
                    .build())
        .toFuture();
  }

  @DgsMutation(field = MUTATION.FavoriteArticle)
  public CompletableFuture<DataFetcherResult<ArticlePayload>> favoriteArticle(
      @InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
              return articleFavoriteRepository.save(articleFavorite).thenReturn(article);
            })
        .map(
            article ->
                DataFetcherResult.<ArticlePayload>newResult()
                    .data(ArticlePayload.newBuilder().build())
                    .localContext(article)
                    .build())
        .toFuture();
  }

  @DgsMutation(field = MUTATION.UnfavoriteArticle)
  public CompletableFuture<DataFetcherResult<ArticlePayload>> unfavoriteArticle(
      @InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article ->
                articleFavoriteRepository
                    .find(article.getId(), user.getId())
                    .flatMap(favorite -> articleFavoriteRepository.remove(favorite))
                    .thenReturn(article))
        .map(
            article ->
                DataFetcherResult.<ArticlePayload>newResult()
                    .data(ArticlePayload.newBuilder().build())
                    .localContext(article)
                    .build())
        .toFuture();
  }

  @DgsMutation(field = MUTATION.DeleteArticle)
  public CompletableFuture<DeletionStatus> deleteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return articleRepository
        .findBySlug(slug)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                return Mono.error(new NoAuthorizationException());
              }
              return articleRepository.remove(article);
            })
        .thenReturn(DeletionStatus.newBuilder().success(true).build())
        .toFuture();
  }
}
