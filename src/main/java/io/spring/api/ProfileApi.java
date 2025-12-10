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
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  @GetMapping
  public Mono<Map<String, Object>> getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return Mono.fromCallable(
            () ->
                profileQueryService
                    .findByUsername(username, user)
                    .map(this::profileResponse)
                    .orElseThrow(ResourceNotFoundException::new))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @PostMapping(path = "follow")
  public Mono<Map<String, Object>> follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return Mono.fromCallable(
            () ->
                userRepository
                    .findByUsername(username)
                    .map(
                        target -> {
                          FollowRelation followRelation =
                              new FollowRelation(user.getId(), target.getId());
                          userRepository.saveRelation(followRelation);
                          return profileResponse(
                              profileQueryService.findByUsername(username, user).get());
                        })
                    .orElseThrow(ResourceNotFoundException::new))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @DeleteMapping(path = "follow")
  public Mono<Map<String, Object>> unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return Mono.fromCallable(
            () -> {
              var userOptional = userRepository.findByUsername(username);
              if (userOptional.isPresent()) {
                User target = userOptional.get();
                return userRepository
                    .findRelation(user.getId(), target.getId())
                    .map(
                        relation -> {
                          userRepository.removeRelation(relation);
                          return profileResponse(
                              profileQueryService.findByUsername(username, user).get());
                        })
                    .orElseThrow(ResourceNotFoundException::new);
              } else {
                throw new ResourceNotFoundException();
              }
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Map<String, Object> profileResponse(ProfileData profile) {
    Map<String, Object> response = new HashMap<>();
    response.put("profile", profile);
    return response;
  }
}
