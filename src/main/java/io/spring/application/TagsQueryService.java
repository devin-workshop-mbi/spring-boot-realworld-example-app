package io.spring.application;

import io.spring.core.article.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagRepository tagRepository;

  public Flux<String> allTags() {
    return tagRepository.findAllTagNames();
  }
}
