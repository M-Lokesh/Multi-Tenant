package com.i2i.tenant.controller;

import com.i2i.tenant.repository.ProductRepository;
import com.i2i.tenant.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> all() {
        return repository.findAll();
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
    }
}
