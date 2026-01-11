package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.model.Product;
import com.mycompany.sapo_leyendo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductController - tests product management:
 * - List all products
 * - Get product by ID
 * - Create product
 * - Update product
 * - Delete product
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setSku("TEST-SKU-001");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test product description");
        testProduct.setWeightKg(1.5);
        testProduct.setIdBaseUom(1);
        testProduct.setMinStockLevel(10);

        testProduct2 = new Product();
        testProduct2.setId(2);
        testProduct2.setSku("TEST-SKU-002");
        testProduct2.setName("Test Product 2");
        testProduct2.setWeightKg(2.0);
        testProduct2.setIdBaseUom(1);
    }

    // ==================== Get All Products Tests ====================

    @Test
    void shouldGetAllProductsSuccessfully() {
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productService.getAllProducts()).thenReturn(products);

        List<Product> result = productController.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TEST-SKU-001", result.get(0).getSku());
        assertEquals("TEST-SKU-002", result.get(1).getSku());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() {
        when(productService.getAllProducts()).thenReturn(new ArrayList<>());

        List<Product> result = productController.getAllProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetManyProducts() {
        List<Product> manyProducts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Product p = new Product();
            p.setId(i);
            p.setSku("SKU-" + i);
            manyProducts.add(p);
        }
        when(productService.getAllProducts()).thenReturn(manyProducts);

        List<Product> result = productController.getAllProducts();

        assertEquals(100, result.size());
    }

    // ==================== Get Product by ID Tests ====================

    @Test
    void shouldGetProductByIdSuccessfully() {
        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));

        ResponseEntity<Product> response = productController.getProductById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("TEST-SKU-001", response.getBody().getSku());
        assertEquals("Test Product", response.getBody().getName());
    }

    @Test
    void shouldReturn404WhenProductNotFound() {
        when(productService.getProductById(999)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = productController.getProductById(999);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void shouldGetProductWithAllDetails() {
        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));

        ResponseEntity<Product> response = productController.getProductById(1);

        Product product = response.getBody();
        assertEquals(1.5, product.getWeightKg());
        assertEquals(10, product.getMinStockLevel());
        assertEquals("Test product description", product.getDescription());
    }

    // ==================== Create Product Tests ====================

    @Test
    void shouldCreateProductSuccessfully() {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        Product result = productController.createProductDirect(testProduct);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("TEST-SKU-001", result.getSku());
        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void shouldCreateProductWithMinimalData() {
        Product minimalProduct = new Product();
        minimalProduct.setSku("MIN-SKU");
        minimalProduct.setName("Minimal Product");
        minimalProduct.setIdBaseUom(1);

        Product savedProduct = new Product();
        savedProduct.setId(10);
        savedProduct.setSku("MIN-SKU");
        savedProduct.setName("Minimal Product");

        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        Product result = productController.createProductDirect(minimalProduct);

        assertNotNull(result.getId());
        assertEquals("MIN-SKU", result.getSku());
    }

    @Test
    void shouldCreateProductWithAllFields() {
        Product fullProduct = new Product();
        fullProduct.setSku("FULL-SKU");
        fullProduct.setName("Full Product");
        fullProduct.setDescription("Full description");
        fullProduct.setWeightKg(5.0);
        fullProduct.setLengthCm(10.0);
        fullProduct.setWidthCm(20.0);
        fullProduct.setHeightCm(30.0);
        fullProduct.setMinStockLevel(50);
        fullProduct.setIdBaseUom(1);

        when(productService.createProduct(any(Product.class))).thenReturn(fullProduct);

        Product result = productController.createProductDirect(fullProduct);

        assertEquals("FULL-SKU", result.getSku());
        assertEquals(5.0, result.getWeightKg());
        assertEquals(50, result.getMinStockLevel());
    }

    // ==================== Update Product Tests ====================

    @Test
    void shouldUpdateProductSuccessfully() {
        Product updatedProduct = new Product();
        updatedProduct.setId(1);
        updatedProduct.setSku("TEST-SKU-001");
        updatedProduct.setName("Updated Product Name");
        updatedProduct.setDescription("Updated description");

        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));
        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(1, updatedProduct);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Product Name", response.getBody().getName());
        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentProduct() {
        Product updateRequest = new Product();
        updateRequest.setName("Updated Name");

        when(productService.getProductById(999)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = productController.updateProduct(999, updateRequest);

        assertEquals(404, response.getStatusCodeValue());
        verify(productService, never()).updateProduct(any(Product.class));
    }

    @Test
    void shouldUpdateProductWeightAndDimensions() {
        Product updatedProduct = new Product();
        updatedProduct.setId(1);
        updatedProduct.setSku("TEST-SKU-001");
        updatedProduct.setWeightKg(3.0);
        updatedProduct.setLengthCm(15.0);
        updatedProduct.setWidthCm(25.0);
        updatedProduct.setHeightCm(35.0);

        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));
        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(1, updatedProduct);

        assertEquals(3.0, response.getBody().getWeightKg());
        assertEquals(15.0, response.getBody().getLengthCm());
    }

    @Test
    void shouldUpdateProductMinStockLevel() {
        Product updatedProduct = new Product();
        updatedProduct.setId(1);
        updatedProduct.setMinStockLevel(100);

        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));
        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        ResponseEntity<Product> response = productController.updateProduct(1, updatedProduct);

        assertEquals(100, response.getBody().getMinStockLevel());
    }

    @Test
    void shouldPreserveIdOnUpdate() {
        Product updateRequest = new Product();
        updateRequest.setName("New Name");
        // ID might be different in request

        when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));
        when(productService.updateProduct(argThat(p -> p.getId() == 1))).thenReturn(testProduct);

        ResponseEntity<Product> response = productController.updateProduct(1, updateRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(productService).updateProduct(argThat(p -> p.getId() == 1));
    }

    // ==================== Delete Product Tests ====================

    @Test
    void shouldDeleteProductSuccessfully() {
        doNothing().when(productService).deleteProduct(1);

        ResponseEntity<Void> response = productController.deleteProduct(1);

        assertEquals(204, response.getStatusCodeValue());
        verify(productService, times(1)).deleteProduct(1);
    }

    @Test
    void shouldDeleteProductEvenIfNotExists() {
        doNothing().when(productService).deleteProduct(999);

        ResponseEntity<Void> response = productController.deleteProduct(999);

        assertEquals(204, response.getStatusCodeValue());
    }

    // ==================== Edge Cases ====================

    @Test
    void shouldHandleProductWithNullFields() {
        Product productWithNulls = new Product();
        productWithNulls.setId(3);
        productWithNulls.setSku("NULL-SKU");
        productWithNulls.setName("Product with nulls");
        // Other fields are null

        when(productService.getProductById(3)).thenReturn(Optional.of(productWithNulls));

        ResponseEntity<Product> response = productController.getProductById(3);

        assertNotNull(response.getBody());
        assertNull(response.getBody().getDescription());
        assertNull(response.getBody().getWeightKg());
    }

    @Test
    void shouldHandleProductWithZeroValues() {
        Product zeroProduct = new Product();
        zeroProduct.setId(4);
        zeroProduct.setSku("ZERO-SKU");
        zeroProduct.setName("Zero Product");
        zeroProduct.setWeightKg(0.0);
        zeroProduct.setMinStockLevel(0);

        when(productService.getProductById(4)).thenReturn(Optional.of(zeroProduct));

        ResponseEntity<Product> response = productController.getProductById(4);

        assertEquals(0.0, response.getBody().getWeightKg());
        assertEquals(0, response.getBody().getMinStockLevel());
    }

    @Test
    void shouldHandleLongProductName() {
        String longName = "This is a very long product name ".repeat(10);
        Product longNameProduct = new Product();
        longNameProduct.setId(5);
        longNameProduct.setSku("LONG-SKU");
        longNameProduct.setName(longName);

        when(productService.createProduct(any(Product.class))).thenReturn(longNameProduct);

        Product result = productController.createProductDirect(longNameProduct);

        assertEquals(longName, result.getName());
    }
}
