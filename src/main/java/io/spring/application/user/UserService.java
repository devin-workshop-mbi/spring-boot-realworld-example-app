package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private UserRepository userRepository;
  private String defaultImage;
  private PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(
      UserRepository userRepository,
      @Value("${image.default}") String defaultImage,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.defaultImage = defaultImage;
    this.passwordEncoder = passwordEncoder;
  }

  public Mono<User> createUser(RegisterParam registerParam) {
    User user =
        new User(
            registerParam.getEmail(),
            registerParam.getUsername(),
            passwordEncoder.encode(registerParam.getPassword()),
            "",
            defaultImage);
    return userRepository.save(user);
  }

  public Mono<User> updateUser(UpdateUserCommand command) {
    User user = command.getTargetUser();
    UpdateUserParam updateUserParam = command.getParam();
    user.update(
        updateUserParam.getEmail(),
        updateUserParam.getUsername(),
        updateUserParam.getPassword(),
        updateUserParam.getBio(),
        updateUserParam.getImage());
    return userRepository.save(user);
  }

  public Mono<Boolean> checkEmailUnique(String email, User targetUser) {
    return userRepository
        .findByEmail(email)
        .map(user -> user.equals(targetUser))
        .defaultIfEmpty(true);
  }

  public Mono<Boolean> checkUsernameUnique(String username, User targetUser) {
    return userRepository
        .findByUsername(username)
        .map(user -> user.equals(targetUser))
        .defaultIfEmpty(true);
  }
}
