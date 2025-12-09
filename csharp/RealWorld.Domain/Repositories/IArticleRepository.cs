using RealWorld.Domain.Entities;

namespace RealWorld.Domain.Repositories;

public interface IArticleRepository
{
    Task SaveAsync(Article article);
    Task<Article?> FindByIdAsync(string id);
    Task<Article?> FindBySlugAsync(string slug);
    Task RemoveAsync(Article article);
}
