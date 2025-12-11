package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.core.article.Article;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.scheduler.Schedulers;

class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, String> {

  @Autowired private ArticleQueryService articleQueryService;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Block here since ConstraintValidator is synchronous by design
    // Use publishOn to switch to boundedElastic scheduler before blocking
    try {
      return articleQueryService
          .findBySlug(Article.toSlug(value), null)
          .publishOn(Schedulers.boundedElastic())
          .toFuture()
          .get() == null;
    } catch (Exception e) {
      return true;
    }
  }
}
