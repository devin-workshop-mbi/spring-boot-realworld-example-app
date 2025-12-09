using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Domain.Services;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("articles/{slug}/comments")]
public class CommentsController : ControllerBase
{
    private readonly IArticleRepository _articleRepository;
    private readonly ICommentRepository _commentRepository;
    private readonly ICommentQueryService _commentQueryService;
    private readonly IUserRepository _userRepository;

    public CommentsController(
        IArticleRepository articleRepository,
        ICommentRepository commentRepository,
        ICommentQueryService commentQueryService,
        IUserRepository userRepository)
    {
        _articleRepository = articleRepository;
        _commentRepository = commentRepository;
        _commentQueryService = commentQueryService;
        _userRepository = userRepository;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<IActionResult> GetComments(string slug)
    {
        var article = await _articleRepository.FindBySlugAsync(slug);
        if (article == null)
        {
            return NotFound();
        }

        var currentUser = await GetCurrentUserAsync();
        var comments = await _commentQueryService.FindByArticleIdAsync(article.Id, currentUser);
        return Ok(new { comments });
    }

    [HttpPost]
    [Authorize]
    public async Task<IActionResult> CreateComment(string slug, [FromBody] NewCommentRequest request)
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

        var comment = new Comment(request.Comment.Body, currentUser.Id, article.Id);
        await _commentRepository.SaveAsync(comment);

        var commentData = await _commentQueryService.FindByIdAsync(comment.Id, currentUser);
        return Ok(new { comment = commentData });
    }

    [HttpDelete("{id}")]
    [Authorize]
    public async Task<IActionResult> DeleteComment(string slug, string id)
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

        var comment = await _commentRepository.FindByIdAsync(article.Id, id);
        if (comment == null)
        {
            return NotFound();
        }

        if (!AuthorizationService.CanWriteComment(currentUser, article, comment))
        {
            return Forbid();
        }

        await _commentRepository.RemoveAsync(comment);
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
