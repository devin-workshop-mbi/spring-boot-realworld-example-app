using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class ArticleData
{
    public string Id { get; set; } = string.Empty;
    public string Slug { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Body { get; set; } = string.Empty;
    public bool Favorited { get; set; }
    public int FavoritesCount { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public List<string> TagList { get; set; } = new();

    [JsonPropertyName("author")]
    public ProfileData Author { get; set; } = new();
}
