using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Domain.Services;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("user")]
[Authorize]
public class UserController : ControllerBase
{
    private readonly IUserRepository _userRepository;
    private readonly IUserService _userService;
    private readonly IJwtService _jwtService;

    public UserController(
        IUserRepository userRepository,
        IUserService userService,
        IJwtService jwtService)
    {
        _userRepository = userRepository;
        _userService = userService;
        _jwtService = jwtService;
    }

    [HttpGet]
    public async Task<IActionResult> GetCurrentUser()
    {
        var user = await GetCurrentUserAsync();
        if (user == null)
        {
            return Unauthorized();
        }

        var token = _jwtService.ToToken(user);

        return Ok(new
        {
            user = new UserWithToken
            {
                Email = user.Email,
                Username = user.Username,
                Bio = user.Bio,
                Image = user.Image,
                Token = token
            }
        });
    }

    [HttpPut]
    public async Task<IActionResult> UpdateUser([FromBody] UpdateUserRequest request)
    {
        var user = await GetCurrentUserAsync();
        if (user == null)
        {
            return Unauthorized();
        }

        if (!string.IsNullOrEmpty(request.User.Email) && request.User.Email != user.Email)
        {
            var existingByEmail = await _userRepository.FindByEmailAsync(request.User.Email);
            if (existingByEmail != null)
            {
                return UnprocessableEntity(new { errors = new { email = new[] { "has already been taken" } } });
            }
        }

        if (!string.IsNullOrEmpty(request.User.Username) && request.User.Username != user.Username)
        {
            var existingByUsername = await _userRepository.FindByUsernameAsync(request.User.Username);
            if (existingByUsername != null)
            {
                return UnprocessableEntity(new { errors = new { username = new[] { "has already been taken" } } });
            }
        }

        await _userService.UpdateUserAsync(user, request.User);
        var token = _jwtService.ToToken(user);

        return Ok(new
        {
            user = new UserWithToken
            {
                Email = user.Email,
                Username = user.Username,
                Bio = user.Bio,
                Image = user.Image,
                Token = token
            }
        });
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
