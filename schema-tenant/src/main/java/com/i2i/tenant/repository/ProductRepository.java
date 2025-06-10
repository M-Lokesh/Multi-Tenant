package com.i2i.tenant.repository;

import com.i2i.tenant.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
