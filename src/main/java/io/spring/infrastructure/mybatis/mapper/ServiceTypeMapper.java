package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.servicetype.ServiceType;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServiceTypeMapper {
  void insert(@Param("serviceType") ServiceType serviceType);

  ServiceType findById(@Param("id") String id);

  List<ServiceType> findAll();
}
