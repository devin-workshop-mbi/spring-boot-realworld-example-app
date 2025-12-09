using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;

namespace RealWorld.Tests.Api;

public class ArticlesApiTests : IClassFixture<CustomWebApplicationFactory<Program>>
{
    private readonly HttpClient _client;
    private readonly CustomWebApplicationFactory<Program> _factory;

    public ArticlesApiTests(CustomWebApplicationFactory<Program> factory)
    {
        _factory = factory;
        _client = factory.CreateClient(new WebApplicationFactoryClientOptions
        {
            AllowAutoRedirect = false
        });
    }

    private async Task<string> CreateUserAndGetToken(string email, string username)
    {
        var registerRequest = new
        {
            user = new
            {
                email = email,
                username = username,
                password = "password123"
            }
        };

        var response = await _client.PostAsJsonAsync("/users", registerRequest);
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        return json.RootElement.GetProperty("user").GetProperty("token").GetString()!;
    }

    [Fact]
    public async Task Should_Get_Empty_Articles_List()
    {
        var response = await _client.GetAsync("/articles");
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        
        Assert.True(json.RootElement.TryGetProperty("articles", out _));
        Assert.True(json.RootElement.TryGetProperty("articlesCount", out _));
    }

    [Fact]
    public async Task Should_Create_Article_With_Auth()
    {
        var token = await CreateUserAndGetToken("article@test.com", "articleuser");
        
        var request = new
        {
            article = new
            {
                title = "Test Article",
                description = "Test Description",
                body = "Test Body",
                tagList = new[] { "test", "article" }
            }
        };

        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Token", token);
        var response = await _client.PostAsJsonAsync("/articles", request);
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        var article = json.RootElement.GetProperty("article");
        
        Assert.Equal("Test Article", article.GetProperty("title").GetString());
        Assert.Equal("Test Description", article.GetProperty("description").GetString());
        Assert.Equal("Test Body", article.GetProperty("body").GetString());
    }

    [Fact]
    public async Task Should_Fail_Create_Article_Without_Auth()
    {
        var request = new
        {
            article = new
            {
                title = "Test Article",
                description = "Test Description",
                body = "Test Body",
                tagList = new[] { "test" }
            }
        };

        _client.DefaultRequestHeaders.Authorization = null;
        var response = await _client.PostAsJsonAsync("/articles", request);
        
        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Should_Get_Article_By_Slug()
    {
        var token = await CreateUserAndGetToken("slug@test.com", "sluguser");
        
        var createRequest = new
        {
            article = new
            {
                title = "Slug Test Article",
                description = "Test Description",
                body = "Test Body",
                tagList = new[] { "test" }
            }
        };

        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Token", token);
        var createResponse = await _client.PostAsJsonAsync("/articles", createRequest);
        var createContent = await createResponse.Content.ReadAsStringAsync();
        var createJson = JsonDocument.Parse(createContent);
        var slug = createJson.RootElement.GetProperty("article").GetProperty("slug").GetString();

        _client.DefaultRequestHeaders.Authorization = null;
        var response = await _client.GetAsync($"/articles/{slug}");
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        var article = json.RootElement.GetProperty("article");
        
        Assert.Equal("Slug Test Article", article.GetProperty("title").GetString());
    }

    [Fact]
    public async Task Should_Update_Article()
    {
        var token = await CreateUserAndGetToken("update@test.com", "updateuser");
        
        var createRequest = new
        {
            article = new
            {
                title = "Original Title",
                description = "Original Description",
                body = "Original Body",
                tagList = new[] { "test" }
            }
        };

        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Token", token);
        var createResponse = await _client.PostAsJsonAsync("/articles", createRequest);
        var createContent = await createResponse.Content.ReadAsStringAsync();
        var createJson = JsonDocument.Parse(createContent);
        var slug = createJson.RootElement.GetProperty("article").GetProperty("slug").GetString();

        var updateRequest = new
        {
            article = new
            {
                title = "Updated Title",
                description = "Updated Description",
                body = "Updated Body"
            }
        };

        var response = await _client.PutAsJsonAsync($"/articles/{slug}", updateRequest);
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        var article = json.RootElement.GetProperty("article");
        
        Assert.Equal("Updated Title", article.GetProperty("title").GetString());
        Assert.Equal("Updated Description", article.GetProperty("description").GetString());
        Assert.Equal("Updated Body", article.GetProperty("body").GetString());
    }

    [Fact]
    public async Task Should_Delete_Article()
    {
        var token = await CreateUserAndGetToken("delete@test.com", "deleteuser");
        
        var createRequest = new
        {
            article = new
            {
                title = "Delete Test Article",
                description = "Test Description",
                body = "Test Body",
                tagList = new[] { "test" }
            }
        };

        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Token", token);
        var createResponse = await _client.PostAsJsonAsync("/articles", createRequest);
        var createContent = await createResponse.Content.ReadAsStringAsync();
        var createJson = JsonDocument.Parse(createContent);
        var slug = createJson.RootElement.GetProperty("article").GetProperty("slug").GetString();

        var response = await _client.DeleteAsync($"/articles/{slug}");
        
        Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
    }
}
