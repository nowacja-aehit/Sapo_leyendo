package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.ProductCreateRequest;
import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductCreateRequest request) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMinStockLevel(request.getMinStock() != null ? request.getMinStock() : 0);
        // Ustaw domyślny UOM = 1 (EA)
        product.setIdBaseUom(1);
        return productService.createProduct(product);
    }

    // Alternate endpoint for backward compatibility - accepts full Product object
    @PostMapping("/direct")
    public Product createProductDirect(@RequestBody Product product) {
        // Ensure required fields are set
        if (product.getIdBaseUom() == null) {
            product.setIdBaseUom(1); // Default UOM
        }
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        return productService.getProductById(id)
                .map(existing -> {
                    product.setId(id);
                    return ResponseEntity.ok(productService.updateProduct(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
