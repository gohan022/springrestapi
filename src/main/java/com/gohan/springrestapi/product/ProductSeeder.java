package com.gohan.springrestapi.product;

import com.github.javafaker.Faker;
import com.gohan.springrestapi.entities.Product;
import com.gohan.springrestapi.entities.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/*@Component*/
@Order(3)
public class ProductSeeder implements CommandLineRunner {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        Faker faker = new Faker();

        System.out.println("Product Category Seeder....");

        categoryRepository.save(new ProductCategory("BOOKS"));

        System.out.println("Product Seeder....");

        Random rand = new Random();
        ProductCategory category = categoryRepository.findById(1L).orElse(null);
        for (int i = 0; i < 50; i++) {
            Product product = new Product();
            product.setSku("BOOK-TECH-10" + i);
            product.setName(faker.book().title());
            product.setDescription(faker.lorem().sentence());
            product.setUnitsInStock(rand.nextInt(100));
            product.setUnitPrice(new BigDecimal(220.3));
            product.setImageUrl("assets/images/products/placeholder.png");
            product.setCategory(category);
            product.setActive(true);
            productRepository.save(product);
        }
    }
}
