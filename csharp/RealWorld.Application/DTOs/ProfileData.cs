using System.Text.Json.Serialization;

namespace RealWorld.Application.DTOs;

public class ProfileData
{
    [JsonIgnore]
    public string Id { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string Bio { get; set; } = string.Empty;
    public string Image { get; set; } = string.Empty;
    public bool Following { get; set; }

    public ProfileData() { }

    public ProfileData(string id, string username, string bio, string image, bool following)
    {
        Id = id;
        Username = username;
        Bio = bio;
        Image = image;
        Following = following;
    }
}
