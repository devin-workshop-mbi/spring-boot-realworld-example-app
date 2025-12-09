namespace RealWorld.Domain.Entities;

public class Comment
{
    public string Id { get; private set; } = string.Empty;
    public string Body { get; private set; } = string.Empty;
    public string UserId { get; private set; } = string.Empty;
    public string ArticleId { get; private set; } = string.Empty;
    public DateTime CreatedAt { get; private set; }

    public User? Author { get; private set; }
    public Article? Article { get; private set; }

    private Comment() { }

    public Comment(string body, string userId, string articleId)
    {
        Id = Guid.NewGuid().ToString();
        Body = body;
        UserId = userId;
        ArticleId = articleId;
        CreatedAt = DateTime.UtcNow;
    }

    public override bool Equals(object? obj)
    {
        if (obj is Comment other)
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
