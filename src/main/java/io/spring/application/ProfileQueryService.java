package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserRepository userRepository;

  public Mono<ProfileData> findByUsername(String username, User currentUser) {
    return userRepository
        .findByUsername(username)
        .flatMap(
            user -> {
              if (currentUser != null) {
                return userRepository
                    .findRelation(currentUser.getId(), user.getId())
                    .map(relation -> true)
                    .defaultIfEmpty(false)
                    .map(
                        isFollowing ->
                            new ProfileData(
                                user.getId(),
                                user.getUsername(),
                                user.getBio(),
                                user.getImage(),
                                isFollowing));
              } else {
                return Mono.just(
                    new ProfileData(
                        user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
              }
            });
  }
}
