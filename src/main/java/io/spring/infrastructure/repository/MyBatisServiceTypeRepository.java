package io.spring.infrastructure.repository;

import io.spring.core.servicetype.ServiceType;
import io.spring.core.servicetype.ServiceTypeRepository;
import io.spring.infrastructure.mybatis.mapper.ServiceTypeMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisServiceTypeRepository implements ServiceTypeRepository {
  private final ServiceTypeMapper serviceTypeMapper;

  @Autowired
  public MyBatisServiceTypeRepository(ServiceTypeMapper serviceTypeMapper) {
    this.serviceTypeMapper = serviceTypeMapper;
  }

  @Override
  public void save(ServiceType serviceType) {
    if (serviceTypeMapper.findById(serviceType.getId()) == null) {
      serviceTypeMapper.insert(serviceType);
    }
  }

  @Override
  public Optional<ServiceType> findById(String id) {
    return Optional.ofNullable(serviceTypeMapper.findById(id));
  }

  @Override
  public List<ServiceType> findAll() {
    return serviceTypeMapper.findAll();
  }
}
