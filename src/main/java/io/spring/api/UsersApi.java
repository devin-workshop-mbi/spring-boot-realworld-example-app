package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@AllArgsConstructor
public class UsersApi {
  private UserRepository userRepository;
  private UserQueryService userQueryService;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private UserService userService;

  @RequestMapping(path = "/users", method = POST)
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Map<String, Object>> createUser(@Valid @RequestBody RegisterParam registerParam) {
    return Mono.fromCallable(
            () -> {
              var user = userService.createUser(registerParam);
              var userData = userQueryService.findById(user.getId()).get();
              return userResponse(new UserWithToken(userData, jwtService.toToken(user)));
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @RequestMapping(path = "/users/login", method = POST)
  public Mono<Map<String, Object>> userLogin(@Valid @RequestBody LoginParam loginParam) {
    return Mono.fromCallable(
            () -> {
              var optional = userRepository.findByEmail(loginParam.getEmail());
              if (optional.isPresent()
                  && passwordEncoder.matches(
                      loginParam.getPassword(), optional.get().getPassword())) {
                var userData = userQueryService.findById(optional.get().getId()).get();
                return userResponse(new UserWithToken(userData, jwtService.toToken(optional.get())));
              } else {
                throw new InvalidAuthenticationException();
              }
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    Map<String, Object> response = new HashMap<>();
    response.put("user", userWithToken);
    return response;
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
