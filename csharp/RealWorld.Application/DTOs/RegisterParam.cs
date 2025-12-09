using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class RegisterRequest
{
    [JsonPropertyName("user")]
    public RegisterParam User { get; set; } = new();
}

public class RegisterParam
{
    public string Email { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}
