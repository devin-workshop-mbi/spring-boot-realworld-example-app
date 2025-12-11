package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(CurrentUserApi.class)
@Import({
  WebSecurityConfig.class,
  JacksonCustomizations.class,
  UserService.class,
  ValidationAutoConfiguration.class,
  BCryptPasswordEncoder.class
})
public class CurrentUserApiTest extends TestWithCurrentUser {

  @Autowired private WebTestClient client;

  @MockBean private UserQueryService userQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void should_get_current_user_with_token() throws Exception {
    when(userQueryService.findById(any())).thenReturn(Mono.just(userData));

    client
        .get()
        .uri("/user")
        .header("Authorization", "Token " + token)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.user.email")
        .isEqualTo(email)
        .jsonPath("$.user.username")
        .isEqualTo(username)
        .jsonPath("$.user.bio")
        .isEqualTo("")
        .jsonPath("$.user.image")
        .isEqualTo(defaultAvatar)
        .jsonPath("$.user.token")
        .isEqualTo(token);
  }

  @Test
  public void should_get_401_without_token() throws Exception {
    client
        .get()
        .uri("/user")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  public void should_get_401_with_invalid_token() throws Exception {
    String invalidToken = "asdfasd";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());
    client
        .get()
        .uri("/user")
        .header("Authorization", "Token " + invalidToken)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  public void should_update_current_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "newusernamee";

    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", newEmail);
    userMap.put("bio", newBio);
    userMap.put("username", newUsername);

    Map<String, Object> param = new HashMap<>();
    param.put("user", userMap);

    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Mono.empty());
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Mono.empty());

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Mono.just(userData));

    client
        .put()
        .uri("/user")
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void should_get_error_if_email_exists_when_update_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "newusernamee";

    Map<String, Object> param = prepareUpdateParam(newEmail, newBio, newUsername);

    when(userRepository.findByEmail(eq(newEmail)))
        .thenReturn(Mono.just(new User(newEmail, "username", "123", "", "")));
    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Mono.empty());

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Mono.just(userData));

    client
        .put()
        .uri("/user")
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.email[0]")
        .isEqualTo("email already exist");
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String newEmail, final String newBio, final String newUsername) {
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", newEmail);
    userMap.put("bio", newBio);
    userMap.put("username", newUsername);

    Map<String, Object> param = new HashMap<>();
    param.put("user", userMap);
    return (HashMap<String, Object>) param;
  }

  @Test
  public void should_get_401_if_not_login() throws Exception {
    Map<String, Object> param = new HashMap<>();
    param.put("user", new HashMap<String, Object>());

    client
        .put()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
