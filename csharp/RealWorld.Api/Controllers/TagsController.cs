using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RealWorld.Application.Services;

namespace RealWorld.Api.Controllers;

[ApiController]
[Route("tags")]
public class TagsController : ControllerBase
{
    private readonly ITagsQueryService _tagsQueryService;

    public TagsController(ITagsQueryService tagsQueryService)
    {
        _tagsQueryService = tagsQueryService;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<IActionResult> GetTags()
    {
        var tags = await _tagsQueryService.GetAllTagsAsync();
        return Ok(new { tags });
    }
}
