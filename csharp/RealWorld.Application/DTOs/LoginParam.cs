using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class LoginRequest
{
    [JsonPropertyName("user")]
    public LoginParam User { get; set; } = new();
}

public class LoginParam
{
    public string Email { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}
