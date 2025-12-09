namespace RealWorld.Domain.Entities;

public class Tag
{
    public string Id { get; private set; } = string.Empty;
    public string Name { get; private set; } = string.Empty;

    public ICollection<ArticleTag> ArticleTags { get; private set; } = new List<ArticleTag>();

    private Tag() { }

    public Tag(string name)
    {
        Id = Guid.NewGuid().ToString();
        Name = name;
    }

    public override bool Equals(object? obj)
    {
        if (obj is Tag other)
        {
            return Name == other.Name;
        }
        return false;
    }

    public override int GetHashCode()
    {
        return Name.GetHashCode();
    }
}
