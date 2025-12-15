package io.spring.core.servicetype;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTypeRepository {
  void save(ServiceType serviceType);

  Optional<ServiceType> findById(String id);

  List<ServiceType> findAll();
}
