using Microsoft.EntityFrameworkCore;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Repositories;

public class UserRepository : IUserRepository
{
    private readonly RealWorldDbContext _context;

    public UserRepository(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task SaveAsync(User user)
    {
        var existingUser = await _context.Users.FindAsync(user.Id);
        if (existingUser == null)
        {
            await _context.Users.AddAsync(user);
        }
        else
        {
            _context.Entry(existingUser).CurrentValues.SetValues(user);
        }
        await _context.SaveChangesAsync();
    }

    public async Task<User?> FindByIdAsync(string id)
    {
        return await _context.Users.FindAsync(id);
    }

    public async Task<User?> FindByUsernameAsync(string username)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Username == username);
    }

    public async Task<User?> FindByEmailAsync(string email)
    {
        return await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
    }

    public async Task SaveRelationAsync(FollowRelation followRelation)
    {
        var existing = await _context.FollowRelations
            .FirstOrDefaultAsync(f => f.UserId == followRelation.UserId && f.TargetId == followRelation.TargetId);
        
        if (existing == null)
        {
            await _context.FollowRelations.AddAsync(followRelation);
            await _context.SaveChangesAsync();
        }
    }

    public async Task<FollowRelation?> FindRelationAsync(string userId, string targetId)
    {
        return await _context.FollowRelations
            .FirstOrDefaultAsync(f => f.UserId == userId && f.TargetId == targetId);
    }

    public async Task RemoveRelationAsync(FollowRelation followRelation)
    {
        _context.FollowRelations.Remove(followRelation);
        await _context.SaveChangesAsync();
    }
}
