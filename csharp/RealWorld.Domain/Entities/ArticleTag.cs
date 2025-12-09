namespace RealWorld.Domain.Entities;

public class ArticleTag
{
    public string ArticleId { get; set; } = string.Empty;
    public string TagId { get; set; } = string.Empty;

    public Article? Article { get; set; }
    public Tag? Tag { get; set; }
}
