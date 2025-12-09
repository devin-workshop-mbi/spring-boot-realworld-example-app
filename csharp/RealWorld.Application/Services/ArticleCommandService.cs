using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;

namespace RealWorld.Application.Services;

public class ArticleCommandService : IArticleCommandService
{
    private readonly IArticleRepository _articleRepository;

    public ArticleCommandService(IArticleRepository articleRepository)
    {
        _articleRepository = articleRepository;
    }

    public async Task<Article> CreateArticleAsync(NewArticleParam newArticleParam, User creator)
    {
        var article = new Article(
            newArticleParam.Title,
            newArticleParam.Description,
            newArticleParam.Body,
            newArticleParam.TagList ?? new List<string>(),
            creator.Id);
        
        await _articleRepository.SaveAsync(article);
        return article;
    }

    public async Task<Article> UpdateArticleAsync(Article article, UpdateArticleParam updateArticleParam)
    {
        article.Update(
            updateArticleParam.Title,
            updateArticleParam.Description,
            updateArticleParam.Body);
        
        await _articleRepository.SaveAsync(article);
        return article;
    }
}
