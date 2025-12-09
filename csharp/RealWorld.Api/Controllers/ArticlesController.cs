using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Domain.Services;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("articles")]
public class ArticlesController : ControllerBase
{
    private readonly IArticleRepository _articleRepository;
    private readonly IArticleCommandService _articleCommandService;
    private readonly IArticleQueryService _articleQueryService;
    private readonly IUserRepository _userRepository;

    public ArticlesController(
        IArticleRepository articleRepository,
        IArticleCommandService articleCommandService,
        IArticleQueryService articleQueryService,
        IUserRepository userRepository)
    {
        _articleRepository = articleRepository;
        _articleCommandService = articleCommandService;
        _articleQueryService = articleQueryService;
        _userRepository = userRepository;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<IActionResult> GetArticles(
        [FromQuery] string? tag,
        [FromQuery] string? author,
        [FromQuery] string? favorited,
        [FromQuery] int offset = 0,
        [FromQuery] int limit = 20)
    {
        var currentUser = await GetCurrentUserAsync();
        var page = new Page(offset, limit);
        var articles = await _articleQueryService.FindRecentArticlesAsync(tag, author, favorited, page, currentUser);
        return Ok(articles);
    }

    [HttpGet("feed")]
    [Authorize]
    public async Task<IActionResult> GetFeed(
        [FromQuery] int offset = 0,
        [FromQuery] int limit = 20)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var page = new Page(offset, limit);
        var articles = await _articleQueryService.FindUserFeedAsync(currentUser, page);
        return Ok(articles);
    }

    [HttpGet("{slug}")]
    [AllowAnonymous]
    public async Task<IActionResult> GetArticle(string slug)
    {
        var currentUser = await GetCurrentUserAsync();
        var article = await _articleQueryService.FindBySlugAsync(slug, currentUser);
        
        if (article == null)
        {
            return NotFound();
        }

        return Ok(new { article });
    }

    [HttpPost]
    [Authorize]
    public async Task<IActionResult> CreateArticle([FromBody] NewArticleRequest request)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var article = await _articleCommandService.CreateArticleAsync(request.Article, currentUser);
        var articleData = await _articleQueryService.FindByIdAsync(article.Id, currentUser);

        return Ok(new { article = articleData });
    }

    [HttpPut("{slug}")]
    [Authorize]
    public async Task<IActionResult> UpdateArticle(string slug, [FromBody] UpdateArticleRequest request)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var article = await _articleRepository.FindBySlugAsync(slug);
        if (article == null)
        {
            return NotFound();
        }

        if (!AuthorizationService.CanWriteArticle(currentUser, article))
        {
            return Forbid();
        }

        await _articleCommandService.UpdateArticleAsync(article, request.Article);
        var articleData = await _articleQueryService.FindByIdAsync(article.Id, currentUser);

        return Ok(new { article = articleData });
    }

    [HttpDelete("{slug}")]
    [Authorize]
    public async Task<IActionResult> DeleteArticle(string slug)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var article = await _articleRepository.FindBySlugAsync(slug);
        if (article == null)
        {
            return NotFound();
        }

        if (!AuthorizationService.CanWriteArticle(currentUser, article))
        {
            return Forbid();
        }

        await _articleRepository.RemoveAsync(article);
        return NoContent();
    }

    private async Task<User?> GetCurrentUserAsync()
    {
        var userId = User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userId))
        {
            return null;
        }
        return await _userRepository.FindByIdAsync(userId);
    }
}
