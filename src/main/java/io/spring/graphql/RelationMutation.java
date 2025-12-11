package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@DgsComponent
@AllArgsConstructor
public class RelationMutation {

  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
  public CompletableFuture<ProfilePayload> follow(@InputArgument("username") String username) {
    User currentUser = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return userRepository
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            target -> {
              FollowRelation followRelation = new FollowRelation(currentUser.getId(), target.getId());
              return userRepository
                  .saveRelation(followRelation)
                  .then(buildProfile(username, currentUser));
            })
        .map(profile -> ProfilePayload.newBuilder().profile(profile).build())
        .toFuture();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
  public CompletableFuture<ProfilePayload> unfollow(@InputArgument("username") String username) {
    User currentUser = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    return userRepository
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            target ->
                userRepository
                    .findRelation(currentUser.getId(), target.getId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                    .flatMap(
                        relation ->
                            userRepository
                                .removeRelation(relation)
                                .then(buildProfile(username, currentUser))))
        .map(profile -> ProfilePayload.newBuilder().profile(profile).build())
        .toFuture();
  }

  private Mono<Profile> buildProfile(String username, User current) {
    return profileQueryService
        .findByUsername(username, current)
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
