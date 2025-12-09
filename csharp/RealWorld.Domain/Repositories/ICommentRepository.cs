using RealWorld.Domain.Entities;

namespace RealWorld.Domain.Repositories;

public interface ICommentRepository
{
    Task SaveAsync(Comment comment);
    Task<Comment?> FindByIdAsync(string articleId, string id);
    Task RemoveAsync(Comment comment);
}
