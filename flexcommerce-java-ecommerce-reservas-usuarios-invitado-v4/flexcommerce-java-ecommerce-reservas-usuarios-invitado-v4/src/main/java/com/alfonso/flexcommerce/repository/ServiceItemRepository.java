package com.alfonso.flexcommerce.repository;

import com.alfonso.flexcommerce.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByActiveTrueOrderByNameAsc();
    List<ServiceItem> findAllByOrderByNameAsc();
}
