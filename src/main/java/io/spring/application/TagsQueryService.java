package io.spring.application;

import io.spring.infrastructure.r2dbc.R2dbcTagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private R2dbcTagRepository tagRepository;

  public Flux<String> allTags() {
    return tagRepository.findAllTagNames();
  }
}
