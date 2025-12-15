package io.spring.application.servicebooking;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("booking")
@AllArgsConstructor
@NoArgsConstructor
public class ServiceBookingParam {
  @NotBlank(message = "can't be empty")
  private String vehicleId;

  @NotBlank(message = "can't be empty")
  private String serviceTypeId;

  @NotNull(message = "can't be empty")
  private String scheduledDate;

  private String notes;
}
