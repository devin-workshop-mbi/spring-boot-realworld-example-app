package io.spring.infrastructure.repository;

import io.spring.core.servicebooking.BookingStatus;
import io.spring.core.servicebooking.ServiceBooking;
import io.spring.core.servicebooking.ServiceBookingRepository;
import io.spring.infrastructure.mybatis.mapper.ServiceBookingMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisServiceBookingRepository implements ServiceBookingRepository {
  private final ServiceBookingMapper serviceBookingMapper;

  @Autowired
  public MyBatisServiceBookingRepository(ServiceBookingMapper serviceBookingMapper) {
    this.serviceBookingMapper = serviceBookingMapper;
  }

  @Override
  public void save(ServiceBooking serviceBooking) {
    if (serviceBookingMapper.findById(serviceBooking.getId()) == null) {
      serviceBookingMapper.insert(serviceBooking);
    } else {
      serviceBookingMapper.update(serviceBooking);
    }
  }

  @Override
  public Optional<ServiceBooking> findById(String id) {
    return Optional.ofNullable(serviceBookingMapper.findById(id));
  }

  @Override
  public List<ServiceBooking> findByUserId(String userId) {
    return serviceBookingMapper.findByUserId(userId);
  }

  @Override
  public List<ServiceBooking> findByVehicleId(String vehicleId) {
    return serviceBookingMapper.findByVehicleId(vehicleId);
  }

  @Override
  public List<ServiceBooking> findByStatus(BookingStatus status) {
    return serviceBookingMapper.findByStatus(status.name());
  }

  @Override
  public void remove(ServiceBooking serviceBooking) {
    serviceBookingMapper.delete(serviceBooking.getId());
  }
}
