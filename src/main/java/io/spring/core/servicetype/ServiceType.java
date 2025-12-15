package io.spring.core.servicetype;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ServiceType {
  private String id;
  private String name;
  private String description;
  private int estimatedDurationMinutes;
  private BigDecimal price;
  private DateTime createdAt;

  public ServiceType(
      String name, String description, int estimatedDurationMinutes, BigDecimal price) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.description = description;
    this.estimatedDurationMinutes = estimatedDurationMinutes;
    this.price = price;
    this.createdAt = new DateTime();
  }
}
