package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(UsersApi.class)
@Import({
  WebSecurityConfig.class,
  UserQueryService.class,
  BCryptPasswordEncoder.class,
  JacksonCustomizations.class
})
public class UsersApiTest {
  @Autowired private WebTestClient client;

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @MockBean private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  private String defaultAvatar;

  @BeforeEach
  public void setUp() throws Exception {
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
  }

  @Test
  public void should_create_user_success() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob";

    when(jwtService.toToken(any())).thenReturn("123");
    User user = new User(email, username, "123", "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(any())).thenReturn(userData);

    when(userService.createUser(any())).thenReturn(user);

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isCreated()
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
        .isEqualTo("123");

    verify(userService).createUser(any());
  }

  @Test
  public void should_show_error_message_for_blank_username() throws Exception {

    String email = "john@jacob.com";
    String username = "";

    Map<String, Object> param = prepareRegisterParameter(email, username);

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.username[0]")
        .isEqualTo("can't be empty");
  }

  @Test
  public void should_show_error_message_for_invalid_email() throws Exception {
    String email = "johnxjacob.com";
    String username = "johnjacob";

    Map<String, Object> param = prepareRegisterParameter(email, username);

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.email[0]")
        .isEqualTo("should be an email");
  }

  @Test
  public void should_show_error_for_duplicated_username() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob";

    when(userRepository.findByUsername(eq(username)))
        .thenReturn(Optional.of(new User(email, username, "123", "bio", "")));
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.username[0]")
        .isEqualTo("duplicated username");
  }

  @Test
  public void should_show_error_for_duplicated_email() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(Optional.of(new User(email, username, "123", "bio", "")));

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.email[0]")
        .isEqualTo("duplicated email");
  }

  private HashMap<String, Object> prepareRegisterParameter(
      final String email, final String username) {
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", email);
    userMap.put("password", "johnnyjacob");
    userMap.put("username", username);

    Map<String, Object> param = new HashMap<>();
    param.put("user", userMap);
    return (HashMap<String, Object>) param;
  }

  @Test
  public void should_login_success() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, passwordEncoder.encode(password), "", defaultAvatar);
    UserData userData = new UserData("123", email, username, "", defaultAvatar);

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(jwtService.toToken(any())).thenReturn("123");

    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", email);
    userMap.put("password", password);

    Map<String, Object> param = new HashMap<>();
    param.put("user", userMap);

    client
        .post()
        .uri("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
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
        .isEqualTo("123");
  }

  @Test
  public void should_fail_login_with_wrong_password() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, password, "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);

    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", email);
    userMap.put("password", "123123");

    Map<String, Object> param = new HashMap<>();
    param.put("user", userMap);

    client
        .post()
        .uri("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("invalid email or password");
  }
}
