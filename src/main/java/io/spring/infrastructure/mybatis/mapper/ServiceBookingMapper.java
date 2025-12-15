package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.servicebooking.ServiceBooking;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServiceBookingMapper {
  void insert(@Param("serviceBooking") ServiceBooking serviceBooking);

  void update(@Param("serviceBooking") ServiceBooking serviceBooking);

  ServiceBooking findById(@Param("id") String id);

  List<ServiceBooking> findByUserId(@Param("userId") String userId);

  List<ServiceBooking> findByVehicleId(@Param("vehicleId") String vehicleId);

  List<ServiceBooking> findByStatus(@Param("status") String status);

  void delete(@Param("id") String id);
}
