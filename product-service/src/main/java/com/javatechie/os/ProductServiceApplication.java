package com.javatechie.os;

import com.javatechie.os.entity.Product;
import com.javatechie.os.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
@RequestMapping("/products")
public class ProductServiceApplication {

    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void initProductsTable() {
        productRepository.saveAll(Stream.of(
                new Product("Bluetooth Keyboard","Bluetooth keyboard with built-in rechargeable battery", "Electronics", "pearl", 2000, 4),
                new Product("Trekking Backpack","Durable trekking backpack with 65L capacity", "Outdoor", "white", 3000, 5),
                new Product("Water Bottle","BPA-free water bottle", "Sports", "copper", 1000, 4),
                new Product("Yoga Mat","Eco-friendly yoga mat", "Sports", "pearl", 2000, 4),
                new Product("Espresso Machine","Automatic espresso machine with frother", "Appliances", "black", 4000, 5)
            ).collect(Collectors.toList()));
    }

	@GetMapping
	public List<Product> getProducts(){
		return productRepository.findAll();
	}

	@GetMapping("/{category}")
	public List<Product> getProductsByCategory(@PathVariable String category){
		return productRepository.findByCategory(category);
	}

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
