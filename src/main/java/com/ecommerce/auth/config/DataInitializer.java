package com.ecommerce.auth.config;

import com.ecommerce.auth.product.entity.Category;
import com.ecommerce.auth.product.entity.Product;
import com.ecommerce.auth.product.repository.CategoryRepository;
import com.ecommerce.auth.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            Category bats = new Category("Bats");
            Category balls = new Category("Balls");
            Category gloves = new Category("Gloves");
            Category pads = new Category("Pads");

            categoryRepository.save(bats);
            categoryRepository.save(balls);
            categoryRepository.save(gloves);
            categoryRepository.save(pads);

            createProduct("Kashmir Willow Bat", "Premium grade Kashmir willow cricket bat perfect for all formats.", new BigDecimal("2999.00"), 50, bats);
            createProduct("English Willow Bat", "Grade A English willow bat used by professionals.", new BigDecimal("8999.00"), 20, bats);
            createProduct("Leather Cricket Ball", "Red 4-piece leather cricket ball for test matches.", new BigDecimal("599.00"), 100, balls);
            createProduct("Batting Gloves", "High density foam batting gloves with sweat absorption.", new BigDecimal("1299.00"), 75, gloves);
            createProduct("Leg Guards", "Lightweight professional leg guards with extreme protection.", new BigDecimal("2499.00"), 40, pads);
        }
    }

    private void createProduct(String name, String desc, BigDecimal price, int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        productRepository.save(p);
    }
}
