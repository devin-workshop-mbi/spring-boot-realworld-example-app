using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Repositories;
using RealWorld.Domain.Services;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("users")]
public class UsersController : ControllerBase
{
    private readonly IUserRepository _userRepository;
    private readonly IUserService _userService;
    private readonly IJwtService _jwtService;

    public UsersController(
        IUserRepository userRepository,
        IUserService userService,
        IJwtService jwtService)
    {
        _userRepository = userRepository;
        _userService = userService;
        _jwtService = jwtService;
    }

    [HttpPost]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        var existingByEmail = await _userRepository.FindByEmailAsync(request.User.Email);
        if (existingByEmail != null)
        {
            return UnprocessableEntity(new { errors = new { email = new[] { "has already been taken" } } });
        }

        var existingByUsername = await _userRepository.FindByUsernameAsync(request.User.Username);
        if (existingByUsername != null)
        {
            return UnprocessableEntity(new { errors = new { username = new[] { "has already been taken" } } });
        }

        var user = await _userService.CreateUserAsync(request.User);
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

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var user = await _userRepository.FindByEmailAsync(request.User.Email);
        if (user == null || !BCrypt.Net.BCrypt.Verify(request.User.Password, user.Password))
        {
            return UnprocessableEntity(new { errors = new { body = new[] { "email or password is invalid" } } });
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
}
