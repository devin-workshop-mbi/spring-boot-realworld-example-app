using Microsoft.EntityFrameworkCore;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Repositories;

public class ArticleFavoriteRepository : IArticleFavoriteRepository
{
    private readonly RealWorldDbContext _context;

    public ArticleFavoriteRepository(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task SaveAsync(ArticleFavorite articleFavorite)
    {
        var existing = await _context.ArticleFavorites
            .FirstOrDefaultAsync(f => f.ArticleId == articleFavorite.ArticleId && f.UserId == articleFavorite.UserId);
        
        if (existing == null)
        {
            await _context.ArticleFavorites.AddAsync(articleFavorite);
            await _context.SaveChangesAsync();
        }
    }

    public async Task<ArticleFavorite?> FindAsync(string articleId, string userId)
    {
        return await _context.ArticleFavorites
            .FirstOrDefaultAsync(f => f.ArticleId == articleId && f.UserId == userId);
    }

    public async Task RemoveAsync(ArticleFavorite favorite)
    {
        _context.ArticleFavorites.Remove(favorite);
        await _context.SaveChangesAsync();
    }
}
