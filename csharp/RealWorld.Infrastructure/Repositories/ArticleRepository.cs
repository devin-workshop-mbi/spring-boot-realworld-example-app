using Microsoft.EntityFrameworkCore;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Repositories;

public class ArticleRepository : IArticleRepository
{
    private readonly RealWorldDbContext _context;

    public ArticleRepository(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task SaveAsync(Article article)
    {
        var existingArticle = await _context.Articles.FindAsync(article.Id);
        if (existingArticle == null)
        {
            foreach (var articleTag in article.ArticleTags)
            {
                var existingTag = await _context.Tags.FirstOrDefaultAsync(t => t.Name == articleTag.Tag!.Name);
                if (existingTag != null)
                {
                    articleTag.Tag = existingTag;
                    articleTag.TagId = existingTag.Id;
                }
                else
                {
                    await _context.Tags.AddAsync(articleTag.Tag!);
                    articleTag.TagId = articleTag.Tag!.Id;
                }
            }
            await _context.Articles.AddAsync(article);
        }
        else
        {
            _context.Entry(existingArticle).CurrentValues.SetValues(article);
        }
        await _context.SaveChangesAsync();
    }

    public async Task<Article?> FindByIdAsync(string id)
    {
        return await _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .FirstOrDefaultAsync(a => a.Id == id);
    }

    public async Task<Article?> FindBySlugAsync(string slug)
    {
        return await _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .FirstOrDefaultAsync(a => a.Slug == slug);
    }

    public async Task RemoveAsync(Article article)
    {
        _context.Articles.Remove(article);
        await _context.SaveChangesAsync();
    }
}
