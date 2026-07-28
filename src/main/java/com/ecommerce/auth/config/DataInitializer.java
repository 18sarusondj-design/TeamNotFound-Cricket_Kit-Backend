package com.ecommerce.auth.config;

import com.ecommerce.auth.product.entity.Category;
import com.ecommerce.auth.product.entity.Product;
import com.ecommerce.auth.product.entity.ProductImage;
import com.ecommerce.auth.product.repository.CategoryRepository;
import com.ecommerce.auth.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Ensure all categories exist
        Category batsAndBalls = getOrCreateCategory("Balls and Bats");
        Category protectiveGear = getOrCreateCategory("Gloves,Gaurd and pads");
        Category boots = getOrCreateCategory("Boots");
        Category helmets = getOrCreateCategory("Helmets");
        Category jersey = getOrCreateCategory("Jersey");

        // If there are less than 15 products, it means we haven't added the new helmets yet
        if (productRepository.count() < 15) {
            
            // Delete old dummy products if any (optional, but let's just keep them and add new ones)
            
            String[] helmetUrls = {
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/TYKA%20Force%20helmate.webp?updatedAt=1785222180527",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/SS%20Classic%20Cricket%20Helmet.jpg?updatedAt=1785222178352",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/NILAYA%20Cricket%20Helmet.jpg?updatedAt=1785222181874",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Nexxa%20helmate.jpg?updatedAt=1785222177735",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/moonwalkr%20helmate.webp?updatedAt=1785222175410",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Gmefvr%20helmate.jpg?updatedAt=1785222175702",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/JJ%20JONEX%20Cricket%20Helmet.jpg?updatedAt=1785222173975",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/SG%20BLAZETECH%20helmate.jpg?updatedAt=1785222177834",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Shrey%20helmate.jpg?updatedAt=1785222177318",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/MYC%20Slog%20Pro%20%20helmate.jpg?updatedAt=1785222176009",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/AASHRAY%20Yonker%20%20helmate.jpg?updatedAt=1785222174066",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Reebok%20helmate.jpg?updatedAt=1785222183552",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Steelbird%20Hitz%20%20Helmates.jpg?updatedAt=1785222173265",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/Gray-Nicolls%20helmate.jpg?updatedAt=1785222176586",
                "https://ik.imagekit.io/StringStackSomu/Helmates/helmates/DSC%20Helmate.jpg?updatedAt=1785222173197"
            };

            String[] helmetNames = {
                "TYKA Force Helmet", "SS Classic Cricket Helmet", "NILAYA Cricket Helmet", "Nexxa Helmet",
                "Moonwalkr Helmet", "Gmefvr Helmet", "JJ JONEX Cricket Helmet", "SG BLAZETECH Helmet",
                "Shrey Helmet", "MYC Slog Pro Helmet", "AASHRAY Yonker Helmet", "Reebok Helmet",
                "Steelbird Hitz Helmet", "Gray-Nicolls Helmet", "DSC Helmet"
            };

            Random random = new Random();
            for (int i = 0; i < helmetUrls.length; i++) {
                // Random price between 1999 and 7999
                int randomPrice = 1999 + random.nextInt(6000);
                // Random stock between 10 and 50
                int randomStock = 10 + random.nextInt(40);
                
                createProduct(helmetNames[i], "Premium professional grade cricket helmet for ultimate head protection.", new BigDecimal(randomPrice + ".00"), randomStock, helmets, helmetUrls[i]);
            }
        }
    }

    private Category getOrCreateCategory(String name) {
        try {
            return categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(name)));
        } catch (Exception e) {
            return categoryRepository.save(new Category(name));
        }
    }

    private void createProduct(String name, String desc, BigDecimal price, int stock, Category category, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        
        ProductImage pi = new ProductImage();
        pi.setImageUrl(imageUrl);
        pi.setProduct(p);
        
        p.setImages(List.of(pi));
        
        productRepository.save(p);
    }
}
