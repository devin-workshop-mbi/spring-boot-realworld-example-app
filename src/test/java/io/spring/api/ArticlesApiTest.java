package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_create_article_success() throws Exception {
    String title = "How to train your dragon";
    String slug = "how-to-train-your-dragon";
    String description = "Ever wonder how?";
    String body = "You have to believe";
    List<String> tagList = asList("reactjs", "angularjs", "dragons");
    Map<String, Object> param = prepareParam(title, description, body, tagList);

    ArticleData articleData =
        new ArticleData(
            "123",
            slug,
            title,
            description,
            body,
            false,
            0,
            new DateTime(),
            new DateTime(),
            tagList,
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleCommandService.createArticle(any(), any()))
        .thenReturn(new Article(title, description, body, tagList, user.getId()));

    when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
        .thenReturn(Optional.empty());

    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(200)
        .body("article.title", equalTo(title))
        .body("article.favorited", equalTo(false))
        .body("article.body", equalTo(body))
        .body("article.favoritesCount", equalTo(0))
        .body("article.author.username", equalTo(user.getUsername()))
        .body("article.author.id", equalTo(null));

    verify(articleCommandService).createArticle(any(), any());
  }

  @Test
  public void should_get_error_message_with_wrong_parameter() throws Exception {
    String title = "How to train your dragon";
    String description = "Ever wonder how?";
    String body = "";
    String[] tagList = {"reactjs", "angularjs", "dragons"};
    Map<String, Object> param = prepareParam(title, description, body, asList(tagList));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .prettyPeek()
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_error_message_with_duplicated_title() {
    String title = "How to train your dragon";
    String slug = "how-to-train-your-dragon";
    String description = "Ever wonder how?";
    String body = "You have to believe";
    String[] tagList = {"reactjs", "angularjs", "dragons"};
    Map<String, Object> param = prepareParam(title, description, body, asList(tagList));

    ArticleData articleData =
        new ArticleData(
            "123",
            slug,
            title,
            description,
            body,
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList(tagList),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
        .thenReturn(Optional.of(articleData));

    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .prettyPeek()
        .then()
        .statusCode(422);
  }

  @Test
  public void should_get_top_articles_success() {
    ArticleData article1 =
        new ArticleData(
            "1",
            "top-article-1",
            "Top Article 1",
            "Most favorited",
            "Content 1",
            false,
            10,
            new DateTime(),
            new DateTime(),
            asList("tag1"),
            new ProfileData("userid1", "author1", "bio1", "image1", false));

    ArticleData article2 =
        new ArticleData(
            "2",
            "top-article-2",
            "Top Article 2",
            "Second most favorited",
            "Content 2",
            false,
            5,
            new DateTime(),
            new DateTime(),
            asList("tag2"),
            new ProfileData("userid2", "author2", "bio2", "image2", false));

    List<ArticleData> articles = asList(article1, article2);
    ArticleDataList articleDataList = new ArticleDataList(articles, 2);

    when(articleQueryService.findTopArticles(any(Page.class), any())).thenReturn(articleDataList);

    given()
        .contentType("application/json")
        .when()
        .get("/articles/top")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2))
        .body("articles[0].title", equalTo("Top Article 1"))
        .body("articles[0].favoritesCount", equalTo(10))
        .body("articles[1].title", equalTo("Top Article 2"))
        .body("articles[1].favoritesCount", equalTo(5));
  }

  @Test
  public void should_get_top_articles_with_pagination() {
    List<ArticleData> articles = new ArrayList<>();
    ArticleDataList articleDataList = new ArticleDataList(articles, 0);

    when(articleQueryService.findTopArticles(any(Page.class), any())).thenReturn(articleDataList);

    given()
        .contentType("application/json")
        .param("limit", "5")
        .param("offset", "0")
        .when()
        .get("/articles/top")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_top_articles_with_authentication() {
    ArticleData article1 =
        new ArticleData(
            "1",
            "top-article-1",
            "Top Article 1",
            "Most favorited",
            "Content 1",
            true,
            10,
            new DateTime(),
            new DateTime(),
            asList("tag1"),
            new ProfileData("userid1", "author1", "bio1", "image1", true));

    List<ArticleData> articles = asList(article1);
    ArticleDataList articleDataList = new ArticleDataList(articles, 1);

    when(articleQueryService.findTopArticles(any(Page.class), eq(user)))
        .thenReturn(articleDataList);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/top")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(1))
        .body("articles[0].favorited", equalTo(true))
        .body("articles[0].author.following", equalTo(true));
  }

  private HashMap<String, Object> prepareParam(
      final String title, final String description, final String body, final List<String> tagList) {
    return new HashMap<String, Object>() {
      {
        put(
            "article",
            new HashMap<String, Object>() {
              {
                put("title", title);
                put("description", description);
                put("body", body);
                put("tagList", tagList);
              }
            });
      }
    };
  }
}
