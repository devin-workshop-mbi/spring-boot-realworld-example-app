package io.spring.core.servicebooking;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ServiceBooking {
  private String id;
  private String userId;
  private String vehicleId;
  private String serviceTypeId;
  private DateTime scheduledDate;
  private BookingStatus status;
  private String notes;
  private DateTime createdAt;
  private DateTime updatedAt;

  public ServiceBooking(
      String userId, String vehicleId, String serviceTypeId, DateTime scheduledDate, String notes) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.vehicleId = vehicleId;
    this.serviceTypeId = serviceTypeId;
    this.scheduledDate = scheduledDate;
    this.status = BookingStatus.PENDING;
    this.notes = notes;
    this.createdAt = new DateTime();
    this.updatedAt = new DateTime();
  }

  public void updateScheduledDate(DateTime scheduledDate) {
    if (scheduledDate != null) {
      this.scheduledDate = scheduledDate;
      this.updatedAt = new DateTime();
    }
  }

  public void updateNotes(String notes) {
    this.notes = notes;
    this.updatedAt = new DateTime();
  }

  public void updateStatus(BookingStatus status) {
    if (status != null) {
      this.status = status;
      this.updatedAt = new DateTime();
    }
  }

  public void confirm() {
    this.status = BookingStatus.CONFIRMED;
    this.updatedAt = new DateTime();
  }

  public void startService() {
    this.status = BookingStatus.IN_PROGRESS;
    this.updatedAt = new DateTime();
  }

  public void complete() {
    this.status = BookingStatus.COMPLETED;
    this.updatedAt = new DateTime();
  }

  public void cancel() {
    this.status = BookingStatus.CANCELLED;
    this.updatedAt = new DateTime();
  }
}
