using RealWorld.Domain.Entities;
using Xunit;

namespace RealWorld.Tests.Domain;

public class ArticleTests
{
    [Fact]
    public void Should_Get_Right_Slug()
    {
        var article = new Article("a new   title", "desc", "body", new List<string> { "java" }, "123");
        Assert.Equal("a-new-title", article.Slug);
    }

    [Fact]
    public void Should_Get_Right_Slug_With_Number_In_Title()
    {
        var article = new Article("a new title 2", "desc", "body", new List<string> { "java" }, "123");
        Assert.Equal("a-new-title-2", article.Slug);
    }

    [Fact]
    public void Should_Get_Lower_Case_Slug()
    {
        var article = new Article("A NEW TITLE", "desc", "body", new List<string> { "java" }, "123");
        Assert.Equal("a-new-title", article.Slug);
    }

    [Fact]
    public void Should_Handle_Commas()
    {
        var article = new Article("what?the.hell,w", "desc", "body", new List<string> { "java" }, "123");
        Assert.Equal("what-the-hell-w", article.Slug);
    }

    [Fact]
    public void Should_Generate_Unique_Id()
    {
        var article1 = new Article("title1", "desc", "body", new List<string>(), "123");
        var article2 = new Article("title2", "desc", "body", new List<string>(), "123");
        Assert.NotEqual(article1.Id, article2.Id);
    }

    [Fact]
    public void Should_Update_Article()
    {
        var article = new Article("original title", "original desc", "original body", new List<string>(), "123");
        var originalSlug = article.Slug;
        
        article.Update("new title", "new desc", "new body");
        
        Assert.Equal("new title", article.Title);
        Assert.Equal("new desc", article.Description);
        Assert.Equal("new body", article.Body);
        Assert.NotEqual(originalSlug, article.Slug);
    }

    [Fact]
    public void Should_Not_Update_When_Null()
    {
        var article = new Article("original title", "original desc", "original body", new List<string>(), "123");
        
        article.Update(null, null, null);
        
        Assert.Equal("original title", article.Title);
        Assert.Equal("original desc", article.Description);
        Assert.Equal("original body", article.Body);
    }

    [Fact]
    public void Should_Create_Article_With_Tags()
    {
        var tags = new List<string> { "java", "spring", "boot" };
        var article = new Article("title", "desc", "body", tags, "123");
        
        Assert.Equal(3, article.ArticleTags.Count);
    }

    [Fact]
    public void Should_Create_Article_With_Unique_Tags()
    {
        var tags = new List<string> { "java", "java", "spring" };
        var article = new Article("title", "desc", "body", tags, "123");
        
        Assert.Equal(2, article.ArticleTags.Count);
    }
}
