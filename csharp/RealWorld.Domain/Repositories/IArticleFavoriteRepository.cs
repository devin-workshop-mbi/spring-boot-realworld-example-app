using RealWorld.Domain.Entities;

namespace RealWorld.Domain.Repositories;

public interface IArticleFavoriteRepository
{
    Task SaveAsync(ArticleFavorite articleFavorite);
    Task<ArticleFavorite?> FindAsync(string articleId, string userId);
    Task RemoveAsync(ArticleFavorite favorite);
}
