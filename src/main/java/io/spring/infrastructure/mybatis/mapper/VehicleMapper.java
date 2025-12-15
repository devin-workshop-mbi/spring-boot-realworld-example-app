package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.vehicle.Vehicle;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehicleMapper {
  void insert(@Param("vehicle") Vehicle vehicle);

  void update(@Param("vehicle") Vehicle vehicle);

  Vehicle findById(@Param("id") String id);

  List<Vehicle> findByUserId(@Param("userId") String userId);

  void delete(@Param("id") String id);
}
