using RealWorld.Domain.Entities;
using Xunit;

namespace RealWorld.Tests.Domain;

public class CommentTests
{
    [Fact]
    public void Should_Create_Comment_With_Id()
    {
        var comment = new Comment("body", "userId", "articleId");
        
        Assert.NotNull(comment.Id);
        Assert.NotEmpty(comment.Id);
    }

    [Fact]
    public void Should_Generate_Unique_Id()
    {
        var comment1 = new Comment("body1", "userId", "articleId");
        var comment2 = new Comment("body2", "userId", "articleId");
        
        Assert.NotEqual(comment1.Id, comment2.Id);
    }

    [Fact]
    public void Should_Set_CreatedAt()
    {
        var before = DateTime.UtcNow;
        var comment = new Comment("body", "userId", "articleId");
        var after = DateTime.UtcNow;
        
        Assert.True(comment.CreatedAt >= before);
        Assert.True(comment.CreatedAt <= after);
    }

    [Fact]
    public void Should_Store_Body()
    {
        var comment = new Comment("test body", "userId", "articleId");
        
        Assert.Equal("test body", comment.Body);
    }

    [Fact]
    public void Should_Store_UserId()
    {
        var comment = new Comment("body", "testUserId", "articleId");
        
        Assert.Equal("testUserId", comment.UserId);
    }

    [Fact]
    public void Should_Store_ArticleId()
    {
        var comment = new Comment("body", "userId", "testArticleId");
        
        Assert.Equal("testArticleId", comment.ArticleId);
    }
}
