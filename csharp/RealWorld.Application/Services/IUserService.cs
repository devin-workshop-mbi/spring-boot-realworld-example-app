using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;

namespace RealWorld.Application.Services;

public interface IUserService
{
    Task<User> CreateUserAsync(RegisterParam registerParam);
    Task UpdateUserAsync(User user, UpdateUserParam updateUserParam);
}
