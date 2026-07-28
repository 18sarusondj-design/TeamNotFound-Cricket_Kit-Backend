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
        if (productRepository.count() == 0) {
            Category batsAndBalls = getOrCreateCategory("Balls and Bats");
            Category protectiveGear = getOrCreateCategory("Gloves,Gaurd and pads");
            Category boots = getOrCreateCategory("Boots");
            Category helmets = getOrCreateCategory("Helmets");
            Category jersey = getOrCreateCategory("Jersey");

            createProduct("Premium English Willow Bat", "Grade A English willow bat for professional play.", new BigDecimal("8999.00"), 20, batsAndBalls);
            createProduct("Red Leather Cricket Ball", "Professional 4-piece leather ball.", new BigDecimal("599.00"), 100, batsAndBalls);
            createProduct("Pro Batting Gloves", "High density foam gloves.", new BigDecimal("1299.00"), 75, protectiveGear);
            createProduct("Test Match Leg Guards", "Lightweight pads with extreme protection.", new BigDecimal("2499.00"), 40, protectiveGear);
            createProduct("Spiked Cricket Boots", "Maximum grip for fast bowlers.", new BigDecimal("3499.00"), 30, boots);
            createProduct("Titanium Visor Helmet", "Maximum head protection.", new BigDecimal("4999.00"), 15, helmets);
            createProduct("Team India ODI Jersey", "Official fan jersey, breathable material.", new BigDecimal("1999.00"), 200, jersey);
        }
    }

    private Category getOrCreateCategory(String name) {
        // Find existing or create new to avoid duplicate key errors if the user manually inserted them
        try {
            return categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(name)));
        } catch (Exception e) {
            return categoryRepository.save(new Category(name));
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
