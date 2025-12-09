using System.Text.RegularExpressions;

namespace RealWorld.Domain.Entities;

public class Article
{
    public string Id { get; private set; } = string.Empty;
    public string UserId { get; private set; } = string.Empty;
    public string Slug { get; private set; } = string.Empty;
    public string Title { get; private set; } = string.Empty;
    public string Description { get; private set; } = string.Empty;
    public string Body { get; private set; } = string.Empty;
    public DateTime CreatedAt { get; private set; }
    public DateTime UpdatedAt { get; private set; }

    public User? Author { get; private set; }
    public ICollection<ArticleTag> ArticleTags { get; private set; } = new List<ArticleTag>();
    public ICollection<Comment> Comments { get; private set; } = new List<Comment>();
    public ICollection<ArticleFavorite> Favorites { get; private set; } = new List<ArticleFavorite>();

    private Article() { }

    public Article(string title, string description, string body, List<string> tagList, string userId)
        : this(title, description, body, tagList, userId, DateTime.UtcNow)
    {
    }

    public Article(string title, string description, string body, List<string> tagList, string userId, DateTime createdAt)
    {
        Id = Guid.NewGuid().ToString();
        Slug = ToSlug(title);
        Title = title;
        Description = description;
        Body = body;
        UserId = userId;
        CreatedAt = createdAt;
        UpdatedAt = createdAt;

        var uniqueTags = tagList.Distinct().ToList();
        foreach (var tagName in uniqueTags)
        {
            ArticleTags.Add(new ArticleTag { ArticleId = Id, Tag = new Tag(tagName) });
        }
    }

    public void Update(string? title, string? description, string? body)
    {
        if (!string.IsNullOrEmpty(title))
        {
            Title = title;
            Slug = ToSlug(title);
            UpdatedAt = DateTime.UtcNow;
        }

        if (!string.IsNullOrEmpty(description))
        {
            Description = description;
            UpdatedAt = DateTime.UtcNow;
        }

        if (!string.IsNullOrEmpty(body))
        {
            Body = body;
            UpdatedAt = DateTime.UtcNow;
        }
    }

    public static string ToSlug(string title)
    {
        return Regex.Replace(title.ToLower(), @"[\&|\uFE30-\uFFA0|\'|\""\s\?\,\.]+", "-");
    }

    public override bool Equals(object? obj)
    {
        if (obj is Article other)
        {
            return Id == other.Id;
        }
        return false;
    }

    public override int GetHashCode()
    {
        return Id.GetHashCode();
    }
}
