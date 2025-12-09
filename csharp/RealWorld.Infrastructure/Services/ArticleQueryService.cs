using Microsoft.EntityFrameworkCore;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Services;

public class ArticleQueryService : IArticleQueryService
{
    private readonly RealWorldDbContext _context;

    public ArticleQueryService(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task<ArticleData?> FindByIdAsync(string id, User? user)
    {
        var article = await _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .FirstOrDefaultAsync(a => a.Id == id);

        if (article == null)
        {
            return null;
        }

        return await MapToArticleDataAsync(article, user);
    }

    public async Task<ArticleData?> FindBySlugAsync(string slug, User? user)
    {
        var article = await _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .FirstOrDefaultAsync(a => a.Slug == slug);

        if (article == null)
        {
            return null;
        }

        return await MapToArticleDataAsync(article, user);
    }

    public async Task<ArticleDataList> FindRecentArticlesAsync(string? tag, string? author, string? favoritedBy, Page page, User? currentUser)
    {
        var query = _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .AsQueryable();

        if (!string.IsNullOrEmpty(tag))
        {
            query = query.Where(a => a.ArticleTags.Any(at => at.Tag!.Name == tag));
        }

        if (!string.IsNullOrEmpty(author))
        {
            query = query.Where(a => a.Author!.Username == author);
        }

        if (!string.IsNullOrEmpty(favoritedBy))
        {
            var favoritedByUser = await _context.Users.FirstOrDefaultAsync(u => u.Username == favoritedBy);
            if (favoritedByUser != null)
            {
                var favoritedArticleIds = await _context.ArticleFavorites
                    .Where(f => f.UserId == favoritedByUser.Id)
                    .Select(f => f.ArticleId)
                    .ToListAsync();
                query = query.Where(a => favoritedArticleIds.Contains(a.Id));
            }
        }

        var totalCount = await query.CountAsync();

        var articles = await query
            .OrderByDescending(a => a.CreatedAt)
            .Skip(page.Offset)
            .Take(page.Limit)
            .ToListAsync();

        var articleDataList = new List<ArticleData>();
        foreach (var article in articles)
        {
            articleDataList.Add(await MapToArticleDataAsync(article, currentUser));
        }

        return new ArticleDataList(articleDataList, totalCount);
    }

    public async Task<ArticleDataList> FindUserFeedAsync(User user, Page page)
    {
        var followedUserIds = await _context.FollowRelations
            .Where(f => f.UserId == user.Id)
            .Select(f => f.TargetId)
            .ToListAsync();

        if (followedUserIds.Count == 0)
        {
            return new ArticleDataList(new List<ArticleData>(), 0);
        }

        var query = _context.Articles
            .Include(a => a.Author)
            .Include(a => a.ArticleTags)
                .ThenInclude(at => at.Tag)
            .Where(a => followedUserIds.Contains(a.UserId));

        var totalCount = await query.CountAsync();

        var articles = await query
            .OrderByDescending(a => a.CreatedAt)
            .Skip(page.Offset)
            .Take(page.Limit)
            .ToListAsync();

        var articleDataList = new List<ArticleData>();
        foreach (var article in articles)
        {
            articleDataList.Add(await MapToArticleDataAsync(article, user));
        }

        return new ArticleDataList(articleDataList, totalCount);
    }

    private async Task<ArticleData> MapToArticleDataAsync(Article article, User? currentUser)
    {
        var isFavorited = false;
        var isFollowing = false;

        if (currentUser != null)
        {
            isFavorited = await _context.ArticleFavorites
                .AnyAsync(f => f.ArticleId == article.Id && f.UserId == currentUser.Id);
            
            isFollowing = await _context.FollowRelations
                .AnyAsync(f => f.UserId == currentUser.Id && f.TargetId == article.UserId);
        }

        var favoritesCount = await _context.ArticleFavorites
            .CountAsync(f => f.ArticleId == article.Id);

        return new ArticleData
        {
            Id = article.Id,
            Slug = article.Slug,
            Title = article.Title,
            Description = article.Description,
            Body = article.Body,
            Favorited = isFavorited,
            FavoritesCount = favoritesCount,
            CreatedAt = article.CreatedAt,
            UpdatedAt = article.UpdatedAt,
            TagList = article.ArticleTags.Select(at => at.Tag!.Name).ToList(),
            Author = new ProfileData
            {
                Id = article.Author!.Id,
                Username = article.Author.Username,
                Bio = article.Author.Bio,
                Image = article.Author.Image,
                Following = isFollowing
            }
        };
    }
}
