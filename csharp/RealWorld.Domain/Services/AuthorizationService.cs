using RealWorld.Domain.Entities;

namespace RealWorld.Domain.Services;

public static class AuthorizationService
{
    public static bool CanWriteArticle(User user, Article article)
    {
        return user.Id == article.UserId;
    }

    public static bool CanWriteComment(User user, Article article, Comment comment)
    {
        return user.Id == article.UserId || user.Id == comment.UserId;
    }
}
