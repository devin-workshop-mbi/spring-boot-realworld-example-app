package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.servicebooking.VehicleParam;
import io.spring.core.user.User;
import io.spring.core.vehicle.Vehicle;
import io.spring.core.vehicle.VehicleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
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
@RequestMapping(path = "/vehicles")
@AllArgsConstructor
public class VehiclesApi {
  private VehicleRepository vehicleRepository;

  @PostMapping
  public ResponseEntity<?> createVehicle(
      @AuthenticationPrincipal User user, @Valid @RequestBody VehicleParam vehicleParam) {
    Vehicle vehicle =
        new Vehicle(
            user.getId(),
            vehicleParam.getMake(),
            vehicleParam.getModel(),
            vehicleParam.getYear(),
            vehicleParam.getLicensePlate(),
            vehicleParam.getVin());
    vehicleRepository.save(vehicle);
    return ResponseEntity.status(201).body(vehicleResponse(vehicle));
  }

  @GetMapping
  public ResponseEntity<?> getVehicles(@AuthenticationPrincipal User user) {
    List<Vehicle> vehicles = vehicleRepository.findByUserId(user.getId());
    return ResponseEntity.ok(vehiclesResponse(vehicles));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getVehicle(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return vehicleRepository
        .findById(id)
        .filter(vehicle -> vehicle.getUserId().equals(user.getId()))
        .map(vehicle -> ResponseEntity.ok(vehicleResponse(vehicle)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateVehicle(
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody VehicleParam vehicleParam) {
    return vehicleRepository
        .findById(id)
        .filter(vehicle -> vehicle.getUserId().equals(user.getId()))
        .map(
            vehicle -> {
              vehicle.update(
                  vehicleParam.getMake(),
                  vehicleParam.getModel(),
                  vehicleParam.getYear(),
                  vehicleParam.getLicensePlate(),
                  vehicleParam.getVin());
              vehicleRepository.save(vehicle);
              return ResponseEntity.ok(vehicleResponse(vehicle));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteVehicle(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return vehicleRepository
        .findById(id)
        .filter(vehicle -> vehicle.getUserId().equals(user.getId()))
        .map(
            vehicle -> {
              vehicleRepository.remove(vehicle);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> vehicleResponse(Vehicle vehicle) {
    return new HashMap<String, Object>() {
      {
        put("vehicle", vehicle);
      }
    };
  }

  private Map<String, Object> vehiclesResponse(List<Vehicle> vehicles) {
    return new HashMap<String, Object>() {
      {
        put("vehicles", vehicles);
        put("vehiclesCount", vehicles.size());
      }
    };
  }
}
