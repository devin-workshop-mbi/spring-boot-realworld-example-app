namespace RealWorld.Domain.Entities;

public class ArticleFavorite
{
    public string ArticleId { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;

    public Article? Article { get; set; }
    public User? User { get; set; }

    public ArticleFavorite() { }

    public ArticleFavorite(string articleId, string userId)
    {
        ArticleId = articleId;
        UserId = userId;
    }

    public override bool Equals(object? obj)
    {
        if (obj is ArticleFavorite other)
        {
            return ArticleId == other.ArticleId && UserId == other.UserId;
        }
        return false;
    }

    public override int GetHashCode()
    {
        return HashCode.Combine(ArticleId, UserId);
    }
}
