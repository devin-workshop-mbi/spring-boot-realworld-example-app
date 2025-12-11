package io.spring.application.user;

import io.spring.core.user.UserRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.scheduler.Schedulers;

class DuplicatedUsernameValidator
    implements ConstraintValidator<DuplicatedUsernameConstraint, String> {

  @Autowired private UserRepository userRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    // Block here since ConstraintValidator is synchronous by design
    // Use publishOn to switch to boundedElastic scheduler before blocking
    try {
      return userRepository
          .findByUsername(value)
          .publishOn(Schedulers.boundedElastic())
          .toFuture()
          .get() == null;
    } catch (Exception e) {
      return true;
    }
  }
}
