using RealWorld.Application.DTOs;
using RealWorld.Domain.Entities;

namespace RealWorld.Application.Services;

public interface ICommentQueryService
{
    Task<CommentData?> FindByIdAsync(string id, User? user);
    Task<List<CommentData>> FindByArticleIdAsync(string articleId, User? user);
}
