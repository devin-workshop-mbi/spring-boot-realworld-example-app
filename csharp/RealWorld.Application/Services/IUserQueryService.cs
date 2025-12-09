using RealWorld.Application.DTOs;

namespace RealWorld.Application.Services;

public interface IUserQueryService
{
    Task<UserData?> FindByIdAsync(string id);
}
