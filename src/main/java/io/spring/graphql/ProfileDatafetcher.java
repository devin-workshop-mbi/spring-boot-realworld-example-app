package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENT;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.DgsConstants.USER;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@DgsComponent
@AllArgsConstructor
public class ProfileDatafetcher {

  private ProfileQueryService profileQueryService;

  @DgsData(parentType = USER.TYPE_NAME, field = USER.Profile)
  public CompletableFuture<Profile> getUserProfile(DataFetchingEnvironment dataFetchingEnvironment) {
    User user = dataFetchingEnvironment.getLocalContext();
    String username = user.getUsername();
    return queryProfileMono(username).toFuture();
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Author)
  public CompletableFuture<Profile> getAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
    Map<String, ArticleData> map = dataFetchingEnvironment.getLocalContext();
    Article article = dataFetchingEnvironment.getSource();
    return queryProfileMono(map.get(article.getSlug()).getProfileData().getUsername()).toFuture();
  }

  @DgsData(parentType = COMMENT.TYPE_NAME, field = COMMENT.Author)
  public CompletableFuture<Profile> getCommentAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
    Comment comment = dataFetchingEnvironment.getSource();
    Map<String, CommentData> map = dataFetchingEnvironment.getLocalContext();
    return queryProfileMono(map.get(comment.getId()).getProfileData().getUsername()).toFuture();
  }

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Profile)
  public CompletableFuture<ProfilePayload> queryProfile(
      @InputArgument("username") String username, DataFetchingEnvironment dataFetchingEnvironment) {
    return queryProfileMono(dataFetchingEnvironment.getArgument("username"))
        .map(profile -> ProfilePayload.newBuilder().profile(profile).build())
        .toFuture();
  }

  private Mono<Profile> queryProfileMono(String username) {
    User current = SecurityUtil.getCurrentUser().orElse(null);
    return profileQueryService
        .findByUsername(username, current)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .map(
            profileData ->
                Profile.newBuilder()
                    .username(profileData.getUsername())
                    .bio(profileData.getBio())
                    .image(profileData.getImage())
                    .following(profileData.isFollowing())
                    .build());
  }
}
