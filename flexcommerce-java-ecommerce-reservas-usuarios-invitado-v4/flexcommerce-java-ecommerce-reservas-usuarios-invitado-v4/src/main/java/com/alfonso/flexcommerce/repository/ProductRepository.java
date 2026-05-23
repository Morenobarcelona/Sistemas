package com.alfonso.flexcommerce.repository;

import com.alfonso.flexcommerce.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByCategoryAscNameAsc();
    List<Product> findTop6ByActiveTrueOrderByIdAsc();
    List<Product> findAllByOrderByCategoryAscNameAsc();
    List<Product> findByStockLessThanEqualOrderByStockAsc(int stock);

    /**
     * Bloqueo pesimista para evitar vender el mismo stock dos veces cuando
     * varios clientes compran simultaneamente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
