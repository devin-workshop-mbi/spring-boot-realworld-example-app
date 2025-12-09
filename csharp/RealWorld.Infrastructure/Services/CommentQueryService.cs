using Microsoft.EntityFrameworkCore;
using RealWorld.Application.DTOs;
using RealWorld.Application.Services;
using RealWorld.Domain.Entities;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Services;

public class CommentQueryService : ICommentQueryService
{
    private readonly RealWorldDbContext _context;

    public CommentQueryService(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task<CommentData?> FindByIdAsync(string id, User? user)
    {
        var comment = await _context.Comments
            .Include(c => c.Author)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (comment == null)
        {
            return null;
        }

        return await MapToCommentDataAsync(comment, user);
    }

    public async Task<List<CommentData>> FindByArticleIdAsync(string articleId, User? user)
    {
        var comments = await _context.Comments
            .Include(c => c.Author)
            .Where(c => c.ArticleId == articleId)
            .OrderByDescending(c => c.CreatedAt)
            .ToListAsync();

        var commentDataList = new List<CommentData>();
        foreach (var comment in comments)
        {
            commentDataList.Add(await MapToCommentDataAsync(comment, user));
        }

        return commentDataList;
    }

    private async Task<CommentData> MapToCommentDataAsync(Comment comment, User? currentUser)
    {
        var isFollowing = false;

        if (currentUser != null)
        {
            isFollowing = await _context.FollowRelations
                .AnyAsync(f => f.UserId == currentUser.Id && f.TargetId == comment.UserId);
        }

        return new CommentData
        {
            Id = comment.Id,
            Body = comment.Body,
            ArticleId = comment.ArticleId,
            CreatedAt = comment.CreatedAt,
            UpdatedAt = comment.CreatedAt,
            Author = new ProfileData
            {
                Id = comment.Author!.Id,
                Username = comment.Author.Username,
                Bio = comment.Author.Bio,
                Image = comment.Author.Image,
                Following = isFollowing
            }
        };
    }
}
