package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class UserQueryService {
  private UserRepository userRepository;

  public Mono<UserData> findById(String id) {
    return userRepository.findById(id).map(this::toUserData);
  }

  private UserData toUserData(User user) {
    return new UserData(
        user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
  }
}
