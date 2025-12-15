package io.spring.application.servicebooking;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("booking")
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingParam {
  private String scheduledDate;
  private String status;
  private String notes;
}
