package io.spring.application.servicebooking;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("vehicle")
@AllArgsConstructor
@NoArgsConstructor
public class VehicleParam {
  @NotBlank(message = "can't be empty")
  private String make;

  @NotBlank(message = "can't be empty")
  private String model;

  @Min(value = 1900, message = "year must be at least 1900")
  @Max(value = 2100, message = "year must be at most 2100")
  private int year;

  private String licensePlate;

  private String vin;
}
