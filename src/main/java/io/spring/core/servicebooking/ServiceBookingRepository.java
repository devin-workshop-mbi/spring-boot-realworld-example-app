package io.spring.core.servicebooking;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceBookingRepository {
  void save(ServiceBooking serviceBooking);

  Optional<ServiceBooking> findById(String id);

  List<ServiceBooking> findByUserId(String userId);

  List<ServiceBooking> findByVehicleId(String vehicleId);

  List<ServiceBooking> findByStatus(BookingStatus status);

  void remove(ServiceBooking serviceBooking);
}
