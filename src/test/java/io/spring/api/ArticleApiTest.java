package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest({ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleApiTest extends TestWithCurrentUser {
  @Autowired private WebTestClient client;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleRepository articleRepository;

  @MockBean ArticleCommandService articleCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void should_read_article_success() throws Exception {
    String slug = "test-new-article";
    DateTime time = new DateTime();
    Article article =
        new Article(
            "Test New Article",
            "Desc",
            "Body",
            Arrays.asList("java", "spring", "jpg"),
            user.getId(),
            time);
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

    when(articleQueryService.findBySlug(eq(slug), eq(null))).thenReturn(Optional.of(articleData));

    client
        .get()
        .uri("/articles/{slug}", slug)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.article.slug")
        .isEqualTo(slug)
        .jsonPath("$.article.body")
        .isEqualTo(articleData.getBody())
        .jsonPath("$.article.createdAt")
        .isEqualTo(ISODateTimeFormat.dateTime().withZoneUTC().print(time));
  }

  @Test
  public void should_404_if_article_not_found() throws Exception {
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.empty());
    client.get().uri("/articles/not-exists").exchange().expectStatus().isNotFound();
  }

  @Test
  public void should_update_article_content_success() throws Exception {
    List<String> tagList = Arrays.asList("java", "spring", "jpg");

    Article originalArticle =
        new Article("old title", "old description", "old body", tagList, user.getId());

    Article updatedArticle =
        new Article("new title", "new description", "new body", tagList, user.getId());

    Map<String, Object> updateParam =
        prepareUpdateParam(
            updatedArticle.getTitle(), updatedArticle.getBody(), updatedArticle.getDescription());

    ArticleData updatedArticleData =
        TestHelper.getArticleDataFromArticleAndUser(updatedArticle, user);

    when(articleRepository.findBySlug(eq(originalArticle.getSlug())))
        .thenReturn(Optional.of(originalArticle));
    when(articleCommandService.updateArticle(eq(originalArticle), any()))
        .thenReturn(updatedArticle);
    when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), eq(user)))
        .thenReturn(Optional.of(updatedArticleData));

    client
        .put()
        .uri("/articles/{slug}", originalArticle.getSlug())
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Token " + token)
        .bodyValue(updateParam)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.article.slug")
        .isEqualTo(updatedArticleData.getSlug());
  }

  @Test
  public void should_get_403_if_not_author_to_update_article() throws Exception {
    String title = "new-title";
    String body = "new body";
    String description = "new description";
    Map<String, Object> updateParam = prepareUpdateParam(title, body, description);

    User anotherUser = new User("test@test.com", "test", "123123", "", "");

    Article article =
        new Article(
            title, description, body, Arrays.asList("java", "spring", "jpg"), anotherUser.getId());

    DateTime time = new DateTime();
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            time,
            time,
            Arrays.asList("joda"),
            new ProfileData(
                anotherUser.getId(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage(),
                false));

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleData));

    client
        .put()
        .uri("/articles/{slug}", article.getSlug())
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Token " + token)
        .bodyValue(updateParam)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  public void should_delete_article_success() throws Exception {
    String title = "title";
    String body = "body";
    String description = "description";

    Article article =
        new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    client
        .delete()
        .uri("/articles/{slug}", article.getSlug())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(articleRepository).remove(eq(article));
  }

  @Test
  public void should_403_if_not_author_delete_article() throws Exception {
    String title = "new-title";
    String body = "new body";
    String description = "new description";

    User anotherUser = new User("test@test.com", "test", "123123", "", "");

    Article article =
        new Article(
            title, description, body, Arrays.asList("java", "spring", "jpg"), anotherUser.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    client
        .delete()
        .uri("/articles/{slug}", article.getSlug())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String title, final String body, final String description) {
    return new HashMap<String, Object>() {
      {
        put(
            "article",
            new HashMap<String, Object>() {
              {
                put("title", title);
                put("body", body);
                put("description", description);
              }
            });
      }
    };
  }
}
