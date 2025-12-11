package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  @GetMapping
  public Mono<Map<String, Object>> getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return profileQueryService
        .findByUsername(username, user)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .map(this::profileResponse);
  }

  @PostMapping(path = "follow")
  public Mono<Map<String, Object>> follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return userRepository
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              return userRepository
                  .saveRelation(followRelation)
                  .then(
                      profileQueryService
                          .findByUsername(username, user)
                          .map(this::profileResponse));
            });
  }

  @DeleteMapping(path = "follow")
  public Mono<Map<String, Object>> unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return userRepository
        .findByUsername(username)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .flatMap(
            target ->
                userRepository
                    .findRelation(user.getId(), target.getId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
                    .flatMap(
                        relation ->
                            userRepository
                                .removeRelation(relation)
                                .then(
                                    profileQueryService
                                        .findByUsername(username, user)
                                        .map(this::profileResponse))));
  }

  private Map<String, Object> profileResponse(ProfileData profile) {
    Map<String, Object> response = new HashMap<>();
    response.put("profile", profile);
    return response;
  }
}
