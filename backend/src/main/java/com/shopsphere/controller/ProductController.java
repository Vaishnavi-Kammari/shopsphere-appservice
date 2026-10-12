package com.shopsphere.controller;

import com.shopsphere.dto.Dtos.CategoryDto;
import com.shopsphere.dto.Dtos.ProductDto;
import com.shopsphere.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only catalog endpoints. */
@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDto> list(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) Long categoryId) {
        return productService.list(search, categoryId);
    }

    @GetMapping("/products/{id}")
    public ProductDto get(@PathVariable Long id) {
        return productService.get(id);
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return productService.listCategories();
    }
}
