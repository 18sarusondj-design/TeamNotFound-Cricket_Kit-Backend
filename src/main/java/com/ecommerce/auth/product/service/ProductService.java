package com.ecommerce.auth.product.service;

import com.ecommerce.auth.product.entity.Category;
import com.ecommerce.auth.product.entity.Product;
import com.ecommerce.auth.product.repository.CategoryRepository;
import com.ecommerce.auth.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
