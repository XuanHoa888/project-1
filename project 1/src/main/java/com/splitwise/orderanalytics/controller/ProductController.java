package com.splitwise.orderanalytics.controller;

import com.splitwise.orderanalytics.entity.Product;
import com.splitwise.orderanalytics.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product createOrUpdateProduct(@RequestBody Product product) {
        return productRepository.findByName(product.getName())
                .map(existingProduct -> {
                    existingProduct.setStockQuantity(product.getStockQuantity());
                    return productRepository.save(existingProduct);
                })
                .orElseGet(() -> productRepository.save(product));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        return productRepository.findById(id)
                .map(product -> {
                    if (payload.containsKey("quantity")) {
                        product.setStockQuantity(payload.get("quantity"));
                        return ResponseEntity.ok(productRepository.save(product));
                    }
                    return ResponseEntity.badRequest().body("Quantity is required");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
