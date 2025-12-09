using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("articles/{slug}/favorite")]
[Authorize]
public class FavoritesController : ControllerBase
{
    private readonly IArticleRepository _articleRepository;
    private readonly IArticleFavoriteRepository _articleFavoriteRepository;
    private readonly IArticleQueryService _articleQueryService;
    private readonly IUserRepository _userRepository;

    public FavoritesController(
        IArticleRepository articleRepository,
        IArticleFavoriteRepository articleFavoriteRepository,
        IArticleQueryService articleQueryService,
        IUserRepository userRepository)
    {
        _articleRepository = articleRepository;
        _articleFavoriteRepository = articleFavoriteRepository;
        _articleQueryService = articleQueryService;
        _userRepository = userRepository;
    }

    [HttpPost]
    public async Task<IActionResult> Favorite(string slug)
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

        var favorite = new ArticleFavorite(article.Id, currentUser.Id);
        await _articleFavoriteRepository.SaveAsync(favorite);

        var articleData = await _articleQueryService.FindByIdAsync(article.Id, currentUser);
        return Ok(new { article = articleData });
    }

    [HttpDelete]
    public async Task<IActionResult> Unfavorite(string slug)
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

        var favorite = await _articleFavoriteRepository.FindAsync(article.Id, currentUser.Id);
        if (favorite != null)
        {
            await _articleFavoriteRepository.RemoveAsync(favorite);
        }

        var articleData = await _articleQueryService.FindByIdAsync(article.Id, currentUser);
        return Ok(new { article = articleData });
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
