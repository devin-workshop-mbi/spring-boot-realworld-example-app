package io.spring.core.vehicle;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository {
  void save(Vehicle vehicle);

  Optional<Vehicle> findById(String id);

  List<Vehicle> findByUserId(String userId);

  void remove(Vehicle vehicle);
}
