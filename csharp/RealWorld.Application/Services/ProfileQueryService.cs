using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;

namespace RealWorld.Application.Services;

public class ProfileQueryService : IProfileQueryService
{
    private readonly IUserRepository _userRepository;

    public ProfileQueryService(IUserRepository userRepository)
    {
        _userRepository = userRepository;
    }

    public async Task<ProfileData?> FindByUsernameAsync(string username, User? currentUser)
    {
        var user = await _userRepository.FindByUsernameAsync(username);
        if (user == null)
        {
            return null;
        }

        var isFollowing = false;
        if (currentUser != null)
        {
            var relation = await _userRepository.FindRelationAsync(currentUser.Id, user.Id);
            isFollowing = relation != null;
        }

        return new ProfileData(
            user.Id,
            user.Username,
            user.Bio,
            user.Image,
            isFollowing);
    }
}
