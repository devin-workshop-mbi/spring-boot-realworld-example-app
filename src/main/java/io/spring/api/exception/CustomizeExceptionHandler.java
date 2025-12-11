package io.spring.api.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class CustomizeExceptionHandler {

  @ExceptionHandler({InvalidRequestException.class})
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  public ErrorResource handleInvalidRequest(InvalidRequestException e) {
    List<FieldErrorResource> errorResources =
        e.getErrors().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

    return new ErrorResource(errorResources);
  }

  @ExceptionHandler(InvalidAuthenticationException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  public Map<String, Object> handleInvalidAuthentication(InvalidAuthenticationException e) {
    Map<String, Object> response = new HashMap<>();
    response.put("message", e.getMessage());
    return response;
  }

  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  public ErrorResource handleWebExchangeBindException(WebExchangeBindException e) {
    List<FieldErrorResource> errorResources =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

    return new ErrorResource(errorResources);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResource handleConstraintViolation(ConstraintViolationException ex) {
    List<FieldErrorResource> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      FieldErrorResource fieldErrorResource =
          new FieldErrorResource(
              violation.getRootBeanClass().getName(),
              getParam(violation.getPropertyPath().toString()),
              violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
              violation.getMessage());
      errors.add(fieldErrorResource);
    }

    return new ErrorResource(errors);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
  public ResponseEntity<Void> handleResourceNotFound(ResourceNotFoundException e) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(NoAuthorizationException.class)
  @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
  public ResponseEntity<Void> handleNoAuthorization(NoAuthorizationException e) {
    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
  }

  private String getParam(String s) {
    String[] splits = s.split("\\.");
    if (splits.length == 1) {
      return s;
    } else {
      return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }
  }
}
