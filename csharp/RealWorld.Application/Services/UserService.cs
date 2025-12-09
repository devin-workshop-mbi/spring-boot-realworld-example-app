using Microsoft.Extensions.Configuration;
using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;

namespace RealWorld.Application.Services;

public class UserService : IUserService
{
    private readonly IUserRepository _userRepository;
    private readonly string _defaultImage;

    public UserService(IUserRepository userRepository, IConfiguration configuration)
    {
        _userRepository = userRepository;
        _defaultImage = configuration["Image:Default"] ?? "https://static.productionready.io/images/smiley-cyrus.jpg";
    }

    public async Task<User> CreateUserAsync(RegisterParam registerParam)
    {
        var hashedPassword = BCrypt.Net.BCrypt.HashPassword(registerParam.Password);
        var user = new User(
            registerParam.Email,
            registerParam.Username,
            hashedPassword,
            "",
            _defaultImage);
        
        await _userRepository.SaveAsync(user);
        return user;
    }

    public async Task UpdateUserAsync(User user, UpdateUserParam updateUserParam)
    {
        var password = updateUserParam.Password;
        if (!string.IsNullOrEmpty(password))
        {
            password = BCrypt.Net.BCrypt.HashPassword(password);
        }

        user.Update(
            updateUserParam.Email,
            updateUserParam.Username,
            password,
            updateUserParam.Bio,
            updateUserParam.Image);
        
        await _userRepository.SaveAsync(user);
    }
}
