namespace RealWorld.Domain.Entities;

public class FollowRelation
{
    public string UserId { get; set; } = string.Empty;
    public string TargetId { get; set; } = string.Empty;

    public User? Follower { get; set; }
    public User? Following { get; set; }

    public FollowRelation() { }

    public FollowRelation(string userId, string targetId)
    {
        UserId = userId;
        TargetId = targetId;
    }
}
