using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class ArticleDataList
{
    [JsonPropertyName("articles")]
    public List<ArticleData> Articles { get; set; } = new();

    [JsonPropertyName("articlesCount")]
    public int ArticlesCount { get; set; }

    public ArticleDataList() { }

    public ArticleDataList(List<ArticleData> articles, int count)
    {
        Articles = articles;
        ArticlesCount = count;
    }
}
