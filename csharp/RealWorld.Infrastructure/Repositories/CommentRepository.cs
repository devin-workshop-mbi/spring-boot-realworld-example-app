using Microsoft.EntityFrameworkCore;
using RealWorld.Domain.Entities;
using RealWorld.Domain.Repositories;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Repositories;

public class CommentRepository : ICommentRepository
{
    private readonly RealWorldDbContext _context;

    public CommentRepository(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task SaveAsync(Comment comment)
    {
        await _context.Comments.AddAsync(comment);
        await _context.SaveChangesAsync();
    }

    public async Task<Comment?> FindByIdAsync(string articleId, string id)
    {
        return await _context.Comments
            .Include(c => c.Author)
            .FirstOrDefaultAsync(c => c.ArticleId == articleId && c.Id == id);
    }

    public async Task RemoveAsync(Comment comment)
    {
        _context.Comments.Remove(comment);
        await _context.SaveChangesAsync();
    }
}
