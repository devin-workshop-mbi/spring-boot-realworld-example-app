package io.spring.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
  private User anotherUser;

  @Autowired private WebTestClient client;

  @MockBean private ProfileQueryService profileQueryService;

  private ProfileData profileData;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    anotherUser = new User("username@test.com", "username", "123", "", "");
    profileData =
        new ProfileData(
            anotherUser.getId(),
            anotherUser.getUsername(),
            anotherUser.getBio(),
            anotherUser.getImage(),
            false);
    when(userRepository.findByUsername(eq(anotherUser.getUsername())))
        .thenReturn(Optional.of(anotherUser));
  }

  @Test
  public void should_get_user_profile_success() throws Exception {
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(null)))
        .thenReturn(Optional.of(profileData));
    client
        .get()
        .uri("/profiles/{username}", profileData.getUsername())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.profile.username")
        .isEqualTo(profileData.getUsername());
  }

  @Test
  public void should_follow_user_success() throws Exception {
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));
    client
        .post()
        .uri("/profiles/{username}/follow", anotherUser.getUsername())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk();
    verify(userRepository).saveRelation(new FollowRelation(user.getId(), anotherUser.getId()));
  }

  @Test
  public void should_unfollow_user_success() throws Exception {
    FollowRelation followRelation = new FollowRelation(user.getId(), anotherUser.getId());
    when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId())))
        .thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    client
        .delete()
        .uri("/profiles/{username}/follow", anotherUser.getUsername())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk();

    verify(userRepository).removeRelation(eq(followRelation));
  }
}
