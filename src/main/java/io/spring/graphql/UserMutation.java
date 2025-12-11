package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import io.spring.graphql.types.CreateUserInput;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DgsComponent
@AllArgsConstructor
public class UserMutation {

  private UserRepository userRepository;
  private PasswordEncoder encryptService;
  private UserService userService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public CompletableFuture<DataFetcherResult<UserResult>> createUser(
      @InputArgument("input") CreateUserInput input) {
    RegisterParam registerParam =
        new RegisterParam(input.getEmail(), input.getUsername(), input.getPassword());
    return userService
        .createUser(registerParam)
        .map(
            user ->
                DataFetcherResult.<UserResult>newResult()
                    .data(UserPayload.newBuilder().build())
                    .localContext(user)
                    .build())
        .toFuture();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public CompletableFuture<DataFetcherResult<UserPayload>> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    return userRepository
        .findByEmail(email)
        .filter(user -> encryptService.matches(password, user.getPassword()))
        .map(
            user ->
                DataFetcherResult.<UserPayload>newResult()
                    .data(UserPayload.newBuilder().build())
                    .localContext(user)
                    .build())
        .switchIfEmpty(
            reactor.core.publisher.Mono.error(new InvalidAuthenticationException()))
        .toFuture();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
  public CompletableFuture<DataFetcherResult<UserPayload>> updateUser(
      @InputArgument("changes") UpdateUserInput updateUserInput) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return CompletableFuture.completedFuture(null);
    }
    io.spring.core.user.User currentUser = (io.spring.core.user.User) authentication.getPrincipal();
    UpdateUserParam param =
        UpdateUserParam.builder()
            .username(updateUserInput.getUsername())
            .email(updateUserInput.getEmail())
            .bio(updateUserInput.getBio())
            .password(updateUserInput.getPassword())
            .image(updateUserInput.getImage())
            .build();

    return userService
        .updateUser(new UpdateUserCommand(currentUser, param))
        .map(
            user ->
                DataFetcherResult.<UserPayload>newResult()
                    .data(UserPayload.newBuilder().build())
                    .localContext(currentUser)
                    .build())
        .toFuture();
  }
}
