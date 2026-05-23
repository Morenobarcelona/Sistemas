package com.alfonso.flexcommerce.repository;

import com.alfonso.flexcommerce.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    List<CustomerOrder> findTop10ByOrderByCreatedAtDesc();
    List<CustomerOrder> findByAccountUsernameOrderByCreatedAtDesc(String accountUsername);
    List<CustomerOrder> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId);
}
