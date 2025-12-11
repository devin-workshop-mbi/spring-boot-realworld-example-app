package io.spring.api;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserQueryService userQueryService;
  private UserService userService;

  @GetMapping
  public Mono<Map<String, Object>> currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    return userQueryService
        .findById(currentUser.getId())
        .map(userData -> userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  @PutMapping
  public Mono<Map<String, Object>> updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    return Mono.zip(
            userService.checkEmailUnique(updateUserParam.getEmail(), currentUser),
            userService.checkUsernameUnique(updateUserParam.getUsername(), currentUser))
        .flatMap(
            tuple -> {
              boolean emailUnique = tuple.getT1();
              boolean usernameUnique = tuple.getT2();
              if (!emailUnique || !usernameUnique) {
                StringBuilder message = new StringBuilder();
                if (!emailUnique) {
                  message.append("email already exist");
                }
                if (!usernameUnique) {
                  if (message.length() > 0) message.append(", ");
                  message.append("username already exist");
                }
                return Mono.error(
                    new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message.toString()));
              }
              return userService
                  .updateUser(new UpdateUserCommand(currentUser, updateUserParam))
                  .flatMap(
                      user ->
                          userQueryService
                              .findById(currentUser.getId())
                              .map(
                                  userData ->
                                      userResponse(new UserWithToken(userData, token.split(" ")[1]))));
            });
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    Map<String, Object> response = new HashMap<>();
    response.put("user", userWithToken);
    return response;
  }
}
