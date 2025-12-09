using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("profiles")]
public class ProfilesController : ControllerBase
{
    private readonly IUserRepository _userRepository;
    private readonly IProfileQueryService _profileQueryService;

    public ProfilesController(
        IUserRepository userRepository,
        IProfileQueryService profileQueryService)
    {
        _userRepository = userRepository;
        _profileQueryService = profileQueryService;
    }

    [HttpGet("{username}")]
    [AllowAnonymous]
    public async Task<IActionResult> GetProfile(string username)
    {
        var currentUser = await GetCurrentUserAsync();
        var profile = await _profileQueryService.FindByUsernameAsync(username, currentUser);
        
        if (profile == null)
        {
            return NotFound();
        }

        return Ok(new { profile });
    }

    [HttpPost("{username}/follow")]
    [Authorize]
    public async Task<IActionResult> Follow(string username)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var targetUser = await _userRepository.FindByUsernameAsync(username);
        if (targetUser == null)
        {
            return NotFound();
        }

        var relation = new FollowRelation(currentUser.Id, targetUser.Id);
        await _userRepository.SaveRelationAsync(relation);

        var profile = await _profileQueryService.FindByUsernameAsync(username, currentUser);
        return Ok(new { profile });
    }

    [HttpDelete("{username}/follow")]
    [Authorize]
    public async Task<IActionResult> Unfollow(string username)
    {
        var currentUser = await GetCurrentUserAsync();
        if (currentUser == null)
        {
            return Unauthorized();
        }

        var targetUser = await _userRepository.FindByUsernameAsync(username);
        if (targetUser == null)
        {
            return NotFound();
        }

        var relation = await _userRepository.FindRelationAsync(currentUser.Id, targetUser.Id);
        if (relation != null)
        {
            await _userRepository.RemoveRelationAsync(relation);
        }

        var profile = await _profileQueryService.FindByUsernameAsync(username, currentUser);
        return Ok(new { profile });
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
