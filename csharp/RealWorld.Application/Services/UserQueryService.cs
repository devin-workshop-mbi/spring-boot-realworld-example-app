using RealWorld.Application.DTOs;
using RealWorld.Domain.Repositories;

namespace RealWorld.Application.Services;

public class UserQueryService : IUserQueryService
{
    private readonly IUserRepository _userRepository;

    public UserQueryService(IUserRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<UserData?> FindByIdAsync(string id)
    {
        var user = await _userRepository.FindByIdAsync(id);
        if (user == null)
        {
            return null;
        }

        return new UserData
        {
            Id = user.Id,
            Email = user.Email,
            Username = user.Username,
            Bio = user.Bio,
            Image = user.Image
        };
    }
}
