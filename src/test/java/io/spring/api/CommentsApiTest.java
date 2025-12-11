package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import java.util.Arrays;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsApiTest extends TestWithCurrentUser {

  @MockBean private ArticleRepository articleRepository;

  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;

  private Article article;
  private CommentData commentData;
  private Comment comment;
  @Autowired private WebTestClient client;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    article = new Article("title", "desc", "body", Arrays.asList("test", "java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Mono.just(article));
    comment = new Comment("comment", user.getId(), article.getId());
    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  }

  @Test
  public void should_create_comment_success() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "comment content");
                  }
                });
          }
        };

    when(commentRepository.save(any(Comment.class))).thenReturn(Mono.just(comment));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Mono.just(commentData));

    client
        .post()
        .uri("/articles/{slug}/comments", article.getSlug())
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Token " + token)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.comment.body")
        .isEqualTo(commentData.getBody());
  }

  @Test
  public void should_get_422_with_empty_body() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "");
                  }
                });
          }
        };

    client
        .post()
        .uri("/articles/{slug}/comments", article.getSlug())
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Token " + token)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.errors.body[0]")
        .isEqualTo("can't be empty");
  }

  @Test
  public void should_get_comments_of_article_success() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Flux.just(commentData));
    client
        .get()
        .uri("/articles/{slug}/comments", article.getSlug())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.comments[0].id")
        .isEqualTo(commentData.getId());
  }

  @Test
  public void should_delete_comment_success() throws Exception {
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Mono.just(comment));
    when(commentRepository.remove(eq(comment))).thenReturn(Mono.empty());

    client
        .delete()
        .uri("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  public void should_get_403_if_not_author_of_article_or_author_of_comment_when_delete_comment()
      throws Exception {
    User anotherUser = new User("other@example.com", "other", "123", "", "");
    when(userRepository.findByUsername(eq(anotherUser.getUsername())))
        .thenReturn(Mono.just(anotherUser));
    when(jwtService.getSubFromToken(any())).thenReturn(Optional.of(anotherUser.getId()));
    when(userRepository.findById(eq(anotherUser.getId())))
        .thenReturn(Mono.just(anotherUser));

    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Mono.just(comment));
    String token = jwtService.toToken(anotherUser);
    when(userRepository.findById(eq(anotherUser.getId()))).thenReturn(Mono.just(anotherUser));
    client
        .delete()
        .uri("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}
