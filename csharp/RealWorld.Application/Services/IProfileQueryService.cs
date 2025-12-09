using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;

namespace RealWorld.Application.Services;

public interface IProfileQueryService
{
    Task<ProfileData?> FindByUsernameAsync(string username, User? currentUser);
}
