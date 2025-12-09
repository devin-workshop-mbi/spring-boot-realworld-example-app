using RealWorld.Domain.Entities;
using Xunit;

namespace RealWorld.Tests.Domain;

public class UserTests
{
    [Fact]
    public void Should_Create_User_With_Id()
    {
        var user = new User("test@test.com", "testuser", "password", "", "");
        
        Assert.NotNull(user.Id);
        Assert.NotEmpty(user.Id);
    }

    [Fact]
    public void Should_Generate_Unique_Id()
    {
        var user1 = new User("test1@test.com", "testuser1", "password", "", "");
        var user2 = new User("test2@test.com", "testuser2", "password", "", "");
        
        Assert.NotEqual(user1.Id, user2.Id);
    }

    [Fact]
    public void Should_Update_User()
    {
        var user = new User("test@test.com", "testuser", "password", "bio", "image");
        
        user.Update("new@test.com", "newuser", "newpassword", "newbio", "newimage");
        
        Assert.Equal("new@test.com", user.Email);
        Assert.Equal("newuser", user.Username);
        Assert.Equal("newpassword", user.Password);
        Assert.Equal("newbio", user.Bio);
        Assert.Equal("newimage", user.Image);
    }

    [Fact]
    public void Should_Not_Update_When_Null()
    {
        var user = new User("test@test.com", "testuser", "password", "bio", "image");
        
        user.Update(null, null, null, null, null);
        
        Assert.Equal("test@test.com", user.Email);
        Assert.Equal("testuser", user.Username);
        Assert.Equal("password", user.Password);
        Assert.Equal("bio", user.Bio);
        Assert.Equal("image", user.Image);
    }

    [Fact]
    public void Should_Not_Update_When_Empty()
    {
        var user = new User("test@test.com", "testuser", "password", "bio", "image");
        
        user.Update("", "", "", "", "");
        
        Assert.Equal("test@test.com", user.Email);
        Assert.Equal("testuser", user.Username);
        Assert.Equal("password", user.Password);
        Assert.Equal("bio", user.Bio);
        Assert.Equal("image", user.Image);
    }

    [Fact]
    public void Should_Be_Equal_By_Id()
    {
        var user1 = new User("test@test.com", "testuser", "password", "", "");
        var user2 = new User("test@test.com", "testuser", "password", "", "");
        
        Assert.NotEqual(user1, user2);
    }
}
