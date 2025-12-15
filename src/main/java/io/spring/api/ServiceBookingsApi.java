package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.servicebooking.ServiceBookingParam;
import io.spring.application.servicebooking.UpdateBookingParam;
import io.spring.core.servicebooking.BookingStatus;
import io.spring.core.servicebooking.ServiceBooking;
import io.spring.core.servicebooking.ServiceBookingRepository;
import io.spring.core.servicetype.ServiceTypeRepository;
import io.spring.core.user.User;
import io.spring.core.vehicle.VehicleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class ServiceBookingsApi {
  private ServiceBookingRepository serviceBookingRepository;
  private VehicleRepository vehicleRepository;
  private ServiceTypeRepository serviceTypeRepository;

  @PostMapping
  public ResponseEntity<?> createBooking(
      @AuthenticationPrincipal User user, @Valid @RequestBody ServiceBookingParam bookingParam) {
    vehicleRepository
        .findById(bookingParam.getVehicleId())
        .filter(vehicle -> vehicle.getUserId().equals(user.getId()))
        .orElseThrow(() -> new ResourceNotFoundException());

    serviceTypeRepository
        .findById(bookingParam.getServiceTypeId())
        .orElseThrow(() -> new ResourceNotFoundException());

    DateTime scheduledDate =
        ISODateTimeFormat.dateTimeParser().parseDateTime(bookingParam.getScheduledDate());

    ServiceBooking booking =
        new ServiceBooking(
            user.getId(),
            bookingParam.getVehicleId(),
            bookingParam.getServiceTypeId(),
            scheduledDate,
            bookingParam.getNotes());
    serviceBookingRepository.save(booking);
    return ResponseEntity.status(201).body(bookingResponse(booking));
  }

  @GetMapping
  public ResponseEntity<?> getBookings(@AuthenticationPrincipal User user) {
    List<ServiceBooking> bookings = serviceBookingRepository.findByUserId(user.getId());
    return ResponseEntity.ok(bookingsResponse(bookings));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getBooking(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return serviceBookingRepository
        .findById(id)
        .filter(booking -> booking.getUserId().equals(user.getId()))
        .map(booking -> ResponseEntity.ok(bookingResponse(booking)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateBooking(
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateBookingParam updateParam) {
    return serviceBookingRepository
        .findById(id)
        .filter(booking -> booking.getUserId().equals(user.getId()))
        .map(
            booking -> {
              if (updateParam.getScheduledDate() != null
                  && !updateParam.getScheduledDate().isEmpty()) {
                DateTime scheduledDate =
                    ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(updateParam.getScheduledDate());
                booking.updateScheduledDate(scheduledDate);
              }
              if (updateParam.getStatus() != null && !updateParam.getStatus().isEmpty()) {
                BookingStatus status = BookingStatus.valueOf(updateParam.getStatus());
                booking.updateStatus(status);
              }
              if (updateParam.getNotes() != null) {
                booking.updateNotes(updateParam.getNotes());
              }
              serviceBookingRepository.save(booking);
              return ResponseEntity.ok(bookingResponse(booking));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping("/{id}/confirm")
  public ResponseEntity<?> confirmBooking(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return serviceBookingRepository
        .findById(id)
        .filter(booking -> booking.getUserId().equals(user.getId()))
        .map(
            booking -> {
              booking.confirm();
              serviceBookingRepository.save(booking);
              return ResponseEntity.ok(bookingResponse(booking));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<?> cancelBooking(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return serviceBookingRepository
        .findById(id)
        .filter(booking -> booking.getUserId().equals(user.getId()))
        .map(
            booking -> {
              booking.cancel();
              serviceBookingRepository.save(booking);
              return ResponseEntity.ok(bookingResponse(booking));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteBooking(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return serviceBookingRepository
        .findById(id)
        .filter(booking -> booking.getUserId().equals(user.getId()))
        .map(
            booking -> {
              serviceBookingRepository.remove(booking);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> bookingResponse(ServiceBooking booking) {
    return new HashMap<String, Object>() {
      {
        put("booking", booking);
      }
    };
  }

  private Map<String, Object> bookingsResponse(List<ServiceBooking> bookings) {
    return new HashMap<String, Object>() {
      {
        put("bookings", bookings);
        put("bookingsCount", bookings.size());
      }
    };
  }
}
