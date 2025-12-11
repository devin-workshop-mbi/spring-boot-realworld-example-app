package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public Mono<Map<String, Object>> getTags() {
    return Mono.fromCallable(() -> tagsQueryService.allTags())
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            tags -> {
              Map<String, Object> response = new HashMap<>();
              response.put("tags", tags);
              return response;
            });
  }
}
