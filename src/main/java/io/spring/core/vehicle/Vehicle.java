package io.spring.core.vehicle;

import io.spring.Util;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Vehicle {
  private String id;
  private String userId;
  private String make;
  private String model;
  private int year;
  private String licensePlate;
  private String vin;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Vehicle(
      String userId, String make, String model, int year, String licensePlate, String vin) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.make = make;
    this.model = model;
    this.year = year;
    this.licensePlate = licensePlate;
    this.vin = vin;
    this.createdAt = new DateTime();
    this.updatedAt = new DateTime();
  }

  public void update(String make, String model, Integer year, String licensePlate, String vin) {
    if (!Util.isEmpty(make)) {
      this.make = make;
    }
    if (!Util.isEmpty(model)) {
      this.model = model;
    }
    if (year != null && year > 0) {
      this.year = year;
    }
    if (!Util.isEmpty(licensePlate)) {
      this.licensePlate = licensePlate;
    }
    if (!Util.isEmpty(vin)) {
      this.vin = vin;
    }
    this.updatedAt = new DateTime();
  }
}
