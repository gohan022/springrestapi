package com.gohan.springrestapi.product;

import com.gohan.springrestapi.entities.ProductCategory;
import org.springframework.data.repository.CrudRepository;

public interface ProductCategoryRepository extends CrudRepository<ProductCategory, Long> {
}
