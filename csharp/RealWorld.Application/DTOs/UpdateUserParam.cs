using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class UpdateUserRequest
{
    [JsonPropertyName("user")]
    public UpdateUserParam User { get; set; } = new();
}

public class UpdateUserParam
{
    public string? Email { get; set; }
    public string? Username { get; set; }
    public string? Password { get; set; }
    public string? Bio { get; set; }
    public string? Image { get; set; }
}
