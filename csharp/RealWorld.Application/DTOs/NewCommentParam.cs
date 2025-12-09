using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class NewCommentRequest
{
    [JsonPropertyName("comment")]
    public NewCommentParam Comment { get; set; } = new();
}

public class NewCommentParam
{
    public string Body { get; set; } = string.Empty;
}
