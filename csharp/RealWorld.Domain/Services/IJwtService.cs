using RealWorld.Domain.Entities;

namespace RealWorld.Domain.Services;

public interface IJwtService
{
    string ToToken(User user);
    string? GetSubFromToken(string token);
}
