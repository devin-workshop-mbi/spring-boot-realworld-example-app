using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;

namespace RealWorld.Application.Services;

public interface IArticleCommandService
{
    Task<Article> CreateArticleAsync(NewArticleParam newArticleParam, User creator);
    Task<Article> UpdateArticleAsync(Article article, UpdateArticleParam updateArticleParam);
}
