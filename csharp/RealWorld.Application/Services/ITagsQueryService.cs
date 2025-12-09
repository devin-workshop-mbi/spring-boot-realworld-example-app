namespace RealWorld.Application.Services;

public interface ITagsQueryService
{
    Task<List<string>> GetAllTagsAsync();
}
