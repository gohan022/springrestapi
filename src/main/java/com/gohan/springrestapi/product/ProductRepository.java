package com.gohan.springrestapi.product;

import com.gohan.springrestapi.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
