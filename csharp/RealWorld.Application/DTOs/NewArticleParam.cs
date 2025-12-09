using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class NewArticleRequest
{
    [JsonPropertyName("article")]
    public NewArticleParam Article { get; set; } = new();
}

public class NewArticleParam
{
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string Body { get; set; } = string.Empty;
    public List<string> TagList { get; set; } = new();
}
