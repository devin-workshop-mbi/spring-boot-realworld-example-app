using Microsoft.EntityFrameworkCore;
using RealWorld.Application.Services;
using RealWorld.Infrastructure.Data;

namespace RealWorld.Infrastructure.Services;

public class TagsQueryService : ITagsQueryService
{
    private readonly RealWorldDbContext _context;

    public TagsQueryService(RealWorldDbContext context)
    {
        _context = context;
    }

    public async Task<List<string>> GetAllTagsAsync()
    {
        return await _context.Tags
            .Select(t => t.Name)
            .Distinct()
            .ToListAsync();
    }
}
