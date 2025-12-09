namespace RealWorld.Application.DTOs;

public class UserWithToken
{
    public string Email { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string Bio { get; set; } = string.Empty;
    public string Image { get; set; } = string.Empty;
    public string Token { get; set; } = string.Empty;

    public UserWithToken() { }

    public UserWithToken(UserData userData, string token)
    {
        Email = userData.Email;
        Username = userData.Username;
        Bio = userData.Bio;
        Image = userData.Image;
        Token = token;
    }
}
