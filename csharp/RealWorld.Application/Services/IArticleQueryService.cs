using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;

namespace RealWorld.Application.Services;

public interface IArticleQueryService
{
    Task<ArticleData?> FindByIdAsync(string id, User? user);
    Task<ArticleData?> FindBySlugAsync(string slug, User? user);
    Task<ArticleDataList> FindRecentArticlesAsync(string? tag, string? author, string? favoritedBy, Page page, User? currentUser);
    Task<ArticleDataList> FindUserFeedAsync(User user, Page page);
}
