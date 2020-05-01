package com.gohan.springrestapi.product;

import com.gohan.springrestapi.entities.Product;
import com.gohan.springrestapi.service.MapValidationErrorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {
    private final ProductService productService;
    private final MapValidationErrorService mapValidationErrorService;

    public ProductRestController(ProductService productService, MapValidationErrorService mapValidationErrorService) {
        this.productService = productService;
        this.mapValidationErrorService = mapValidationErrorService;
    }

    @GetMapping("")
    public Page<Product> index(@RequestParam(required = false, defaultValue = "0") int page,
                               @RequestParam(required = false, defaultValue = "15") int size) {
        return productService.findAll(PageRequest.of(page, size));
    }
}
