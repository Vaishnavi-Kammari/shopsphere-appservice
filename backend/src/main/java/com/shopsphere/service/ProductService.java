package com.shopsphere.service;

import com.shopsphere.dto.Dtos.CategoryDto;
import com.shopsphere.dto.Dtos.ProductDto;
import com.shopsphere.dto.Dtos.ProductRequest;
import com.shopsphere.entity.Category;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.CartItemRepository;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.repository.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final CartItemRepository cartItems;

    public ProductService(ProductRepository products, CategoryRepository categories, CartItemRepository cartItems) {
        this.products = products;
        this.categories = categories;
        this.cartItems = cartItems;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> list(String search, Long categoryId) {
        String q = search == null ? "" : search.trim();
        List<Product> found = categoryId == null
                ? products.search(q)
                : products.searchInCategory(q, categoryId);
        return found.stream().map(ProductService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        return toDto(find(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listCategories() {
        return categories.findAll().stream().map(c -> new CategoryDto(c.getId(), c.getName())).toList();
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toDto(products.save(product));
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product product = find(id);
        apply(product, request);
        return toDto(products.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        cartItems.deleteByProduct_Id(id);   // remove it from any shopping carts first
        products.delete(product);           // past orders keep their own copy of name/price
    }

    private Product find(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void apply(Product product, ProductRequest r) {
        Category category = categories.findById(r.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category does not exist"));
        product.setName(r.name().trim());
        product.setDescription(r.description() == null ? null : r.description().trim());
        product.setPrice(r.price());
        product.setStock(r.stock());
        String image = r.imageUrl() == null ? "" : r.imageUrl().trim();
        product.setImageUrl(image.isEmpty() ? null : image);
        product.setCategory(category);
    }

    public static ProductDto toDto(Product p) {
        Category c = p.getCategory();
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                p.getImageUrl(), c == null ? null : c.getId(), c == null ? null : c.getName());
    }
}
