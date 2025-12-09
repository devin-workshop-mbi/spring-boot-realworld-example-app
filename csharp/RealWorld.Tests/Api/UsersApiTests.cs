using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;

namespace RealWorld.Tests.Api;

public class UsersApiTests : IClassFixture<CustomWebApplicationFactory<Program>>
{
    private readonly HttpClient _client;
    private readonly CustomWebApplicationFactory<Program> _factory;

    public UsersApiTests(CustomWebApplicationFactory<Program> factory)
    {
        _factory = factory;
        _client = factory.CreateClient(new WebApplicationFactoryClientOptions
        {
            AllowAutoRedirect = false
        });
    }

    [Fact]
    public async Task Should_Create_User_Success()
    {
        var request = new
        {
            user = new
            {
                email = "john@jacob.com",
                username = "johnjacob",
                password = "johnnyjacob"
            }
        };

        var response = await _client.PostAsJsonAsync("/users", request);
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        var user = json.RootElement.GetProperty("user");
        
        Assert.Equal("john@jacob.com", user.GetProperty("email").GetString());
        Assert.Equal("johnjacob", user.GetProperty("username").GetString());
        Assert.NotNull(user.GetProperty("token").GetString());
    }

    [Fact]
    public async Task Should_Show_Error_For_Duplicated_Email()
    {
        var request1 = new
        {
            user = new
            {
                email = "duplicate@test.com",
                username = "user1",
                password = "password123"
            }
        };

        await _client.PostAsJsonAsync("/users", request1);

        var request2 = new
        {
            user = new
            {
                email = "duplicate@test.com",
                username = "user2",
                password = "password123"
            }
        };

        var response = await _client.PostAsJsonAsync("/users", request2);
        
        Assert.Equal(HttpStatusCode.UnprocessableEntity, response.StatusCode);
    }

    [Fact]
    public async Task Should_Show_Error_For_Duplicated_Username()
    {
        var request1 = new
        {
            user = new
            {
                email = "user3@test.com",
                username = "duplicateuser",
                password = "password123"
            }
        };

        await _client.PostAsJsonAsync("/users", request1);

        var request2 = new
        {
            user = new
            {
                email = "user4@test.com",
                username = "duplicateuser",
                password = "password123"
            }
        };

        var response = await _client.PostAsJsonAsync("/users", request2);
        
        Assert.Equal(HttpStatusCode.UnprocessableEntity, response.StatusCode);
    }

    [Fact]
    public async Task Should_Login_Success()
    {
        var registerRequest = new
        {
            user = new
            {
                email = "login@test.com",
                username = "loginuser",
                password = "password123"
            }
        };

        await _client.PostAsJsonAsync("/users", registerRequest);

        var loginRequest = new
        {
            user = new
            {
                email = "login@test.com",
                password = "password123"
            }
        };

        var response = await _client.PostAsJsonAsync("/users/login", loginRequest);
        
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        
        var content = await response.Content.ReadAsStringAsync();
        var json = JsonDocument.Parse(content);
        var user = json.RootElement.GetProperty("user");
        
        Assert.Equal("login@test.com", user.GetProperty("email").GetString());
        Assert.Equal("loginuser", user.GetProperty("username").GetString());
        Assert.NotNull(user.GetProperty("token").GetString());
    }

    [Fact]
    public async Task Should_Fail_Login_With_Wrong_Password()
    {
        var registerRequest = new
        {
            user = new
            {
                email = "wrongpass@test.com",
                username = "wrongpassuser",
                password = "password123"
            }
        };

        await _client.PostAsJsonAsync("/users", registerRequest);

        var loginRequest = new
        {
            user = new
            {
                email = "wrongpass@test.com",
                password = "wrongpassword"
            }
        };

        var response = await _client.PostAsJsonAsync("/users/login", loginRequest);
        
        Assert.Equal(HttpStatusCode.UnprocessableEntity, response.StatusCode);
    }
}
