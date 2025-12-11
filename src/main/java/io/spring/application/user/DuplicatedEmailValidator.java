package io.spring.application.user;

import io.spring.core.user.UserRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.scheduler.Schedulers;

public class DuplicatedEmailValidator
    implements ConstraintValidator<DuplicatedEmailConstraint, String> {

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
          .findByEmail(value)
          .publishOn(Schedulers.boundedElastic())
          .toFuture()
          .get() == null;
    } catch (Exception e) {
      return true;
    }
  }
}
