package com.shopsphere.config;

import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.Category;
import com.shopsphere.entity.Product;
import com.shopsphere.entity.Role;
import com.shopsphere.repository.CategoryRepository;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the admin account (always) and sample data (only when the database is empty). */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.seed.demo-data}") private boolean seedDemoData;

    public DataSeeder(UserRepository users, CategoryRepository categories,
                      ProductRepository products, PasswordEncoder encoder) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String admin = adminEmail.trim().toLowerCase();
        if (!users.existsByEmail(admin)) {
            users.save(new AppUser("Store Admin", admin, encoder.encode(adminPassword), Role.ADMIN));
            log.info("Created admin user {}", admin);
        }
        if (!seedDemoData) return;

        if (!users.existsByEmail("customer@shopsphere.local")) {
            users.save(new AppUser("Demo Customer", "customer@shopsphere.local",
                    encoder.encode("Customer@123"), Role.CUSTOMER));
            log.info("Created demo customer customer@shopsphere.local");
        }
        if (categories.count() > 0 || products.count() > 0) return;

        Map<String, Category> cat = new LinkedHashMap<>();
        for (String name : new String[] {"Electronics", "Books", "Home & Kitchen", "Fashion", "Sports & Outdoors"}) {
            cat.put(name, categories.save(new Category(name)));
        }

        add(cat.get("Electronics"), "Wireless Noise-Cancelling Headphones",
            "Over-ear Bluetooth headphones with active noise cancellation and 30 hours of battery life.", "129.99", 25);
        add(cat.get("Electronics"), "Mechanical Keyboard",
            "Compact hot-swappable keyboard with tactile switches and white backlighting.", "89.00", 40);
        add(cat.get("Electronics"), "27-inch 4K Monitor",
            "Sharp IPS display with thin bezels, USB-C input and an adjustable stand.", "349.50", 12);
        add(cat.get("Electronics"), "Portable Bluetooth Speaker",
            "Water-resistant pocket speaker with surprisingly deep bass and 12 hours of playtime.", "59.99", 60);
        add(cat.get("Books"), "Cloud Fundamentals Handbook",
            "A friendly introduction to virtual machines, networking, storage and managed databases.", "34.50", 30);
        add(cat.get("Books"), "The Art of Clean Code",
            "Practical habits for writing readable, testable and maintainable software.", "42.00", 18);
        add(cat.get("Home & Kitchen"), "Stainless Steel Water Bottle",
            "Double-wall insulated bottle that keeps drinks cold for 24 hours or hot for 12.", "24.99", 80);
        add(cat.get("Home & Kitchen"), "Pour-Over Coffee Set",
            "Glass dripper, carafe and reusable filter for a clean, bright cup at home.", "39.95", 22);
        add(cat.get("Home & Kitchen"), "Cast Iron Skillet",
            "Pre-seasoned 10-inch skillet that goes from stovetop to oven to campfire.", "31.00", 35);
        add(cat.get("Fashion"), "Classic Denim Jacket",
            "Mid-weight washed denim with a relaxed fit and brushed-metal buttons.", "79.00", 15);
        add(cat.get("Fashion"), "Canvas Backpack",
            "Roomy 20-litre daypack with a padded laptop sleeve and leather trim.", "49.90", 45);
        add(cat.get("Sports & Outdoors"), "Non-Slip Yoga Mat",
            "6 mm cushioned mat with a grippy surface and a carrying strap.", "27.50", 50);
        add(cat.get("Sports & Outdoors"), "Adjustable Dumbbell Set",
            "Pair of dumbbells adjustable from 2 to 20 kg with a quick-lock dial.", "119.00", 8);
        add(cat.get("Sports & Outdoors"), "Trail Running Shoes",
            "Lightweight shoes with aggressive tread and a breathable mesh upper.", "94.00", 0);

        log.info("Seeded {} categories and {} products", categories.count(), products.count());
    }

    private int counter = 0;

    private void add(Category category, String name, String description, String price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setImageUrl("https://picsum.photos/seed/shopsphere-" + (++counter) + "/640/480");
        p.setCategory(category);
        products.save(p);
    }
}
