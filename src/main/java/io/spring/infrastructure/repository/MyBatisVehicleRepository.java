package io.spring.infrastructure.repository;

import io.spring.core.vehicle.Vehicle;
import io.spring.core.vehicle.VehicleRepository;
import io.spring.infrastructure.mybatis.mapper.VehicleMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisVehicleRepository implements VehicleRepository {
  private final VehicleMapper vehicleMapper;

  @Autowired
  public MyBatisVehicleRepository(VehicleMapper vehicleMapper) {
    this.vehicleMapper = vehicleMapper;
  }

  @Override
  public void save(Vehicle vehicle) {
    if (vehicleMapper.findById(vehicle.getId()) == null) {
      vehicleMapper.insert(vehicle);
    } else {
      vehicleMapper.update(vehicle);
    }
  }

  @Override
  public Optional<Vehicle> findById(String id) {
    return Optional.ofNullable(vehicleMapper.findById(id));
  }

  @Override
  public List<Vehicle> findByUserId(String userId) {
    return vehicleMapper.findByUserId(userId);
  }

  @Override
  public void remove(Vehicle vehicle) {
    vehicleMapper.delete(vehicle.getId());
  }
}
