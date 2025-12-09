namespace RealWorld.Domain.Entities;

public class User
{
    public string Id { get; private set; } = string.Empty;
    public string Email { get; private set; } = string.Empty;
    public string Username { get; private set; } = string.Empty;
    public string Password { get; private set; } = string.Empty;
    public string Bio { get; private set; } = string.Empty;
    public string Image { get; private set; } = string.Empty;

    public ICollection<FollowRelation> Following { get; private set; } = new List<FollowRelation>();
    public ICollection<FollowRelation> Followers { get; private set; } = new List<FollowRelation>();
    public ICollection<Article> Articles { get; private set; } = new List<Article>();
    public ICollection<Comment> Comments { get; private set; } = new List<Comment>();
    public ICollection<ArticleFavorite> Favorites { get; private set; } = new List<ArticleFavorite>();

    private User() { }

    public User(string email, string username, string password, string bio, string image)
    {
        Id = Guid.NewGuid().ToString();
        Email = email;
        Username = username;
        Password = password;
        Bio = bio;
        Image = image;
    }

    public void Update(string? email, string? username, string? password, string? bio, string? image)
    {
        if (!string.IsNullOrEmpty(email))
        {
            Email = email;
        }

        if (!string.IsNullOrEmpty(username))
        {
            Username = username;
        }

        if (!string.IsNullOrEmpty(password))
        {
            Password = password;
        }

        if (!string.IsNullOrEmpty(bio))
        {
            Bio = bio;
        }

        if (!string.IsNullOrEmpty(image))
        {
            Image = image;
        }
    }

    public override bool Equals(object? obj)
    {
        if (obj is User other)
        {
            return Id == other.Id;
        }
        return false;
    }

    public override int GetHashCode()
    {
        return Id.GetHashCode();
    }
}
