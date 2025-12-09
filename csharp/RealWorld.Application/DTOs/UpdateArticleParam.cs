using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class UpdateArticleRequest
{
    [JsonPropertyName("article")]
    public UpdateArticleParam Article { get; set; } = new();
}

public class UpdateArticleParam
{
    public string? Title { get; set; }
    public string? Description { get; set; }
    public string? Body { get; set; }
}
