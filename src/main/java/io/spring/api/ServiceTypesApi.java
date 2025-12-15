package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.servicetype.ServiceType;
import io.spring.core.servicetype.ServiceTypeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/service-types")
@AllArgsConstructor
public class ServiceTypesApi {
  private ServiceTypeRepository serviceTypeRepository;

  @GetMapping
  public ResponseEntity<?> getServiceTypes() {
    List<ServiceType> serviceTypes = serviceTypeRepository.findAll();
    return ResponseEntity.ok(serviceTypesResponse(serviceTypes));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getServiceType(@PathVariable("id") String id) {
    return serviceTypeRepository
        .findById(id)
        .map(serviceType -> ResponseEntity.ok(serviceTypeResponse(serviceType)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> serviceTypeResponse(ServiceType serviceType) {
    return new HashMap<String, Object>() {
      {
        put("serviceType", serviceType);
      }
    };
  }

  private Map<String, Object> serviceTypesResponse(List<ServiceType> serviceTypes) {
    return new HashMap<String, Object>() {
      {
        put("serviceTypes", serviceTypes);
        put("serviceTypesCount", serviceTypes.size());
      }
    };
  }
}
