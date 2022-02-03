package com.javatechie.os;

import com.javatechie.os.entity.Order;
import com.javatechie.os.repository.OrderRepository;
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
@RequestMapping("/orders")
public class CatalogServiceApplication {

    @Autowired
    private OrderRepository orderRepository;

    @PostConstruct
    public void initOrdersTable() {
        orderRepository.saveAll(Stream.of(
                        new Order("mobile", "electronics", 2, 40000),
                        new Order("T-Shirt", "clothes", 5, 5000),
                        new Order("Jeans", "clothes", 2, 3000),
                        new Order("Laptop", "electronics", 1, 50000),
                        new Order("digital watch", "electronics", 1, 2500),
                        new Order("Fan", "electronics", 3, 50000)
                ).
                collect(Collectors.toList()));
    }

	@GetMapping
	public List<Order> getOrders(){
		return orderRepository.findAll();
	}
  @GetMapping("/bulkhead")
  public List<Order> getBulkheadOrders(){
    return orderRepository.findAll();
  }
  @GetMapping("/threadPoolBulkhead")
  public List<Order> getThreadPoolBulkheadOrders(){
    return orderRepository.findAll();
  }

	@GetMapping("/{category}")
	public List<Order> getOrdersByCategory(@PathVariable String category){
		return orderRepository.findByCategory(category);
	}
  @GetMapping("/bulkhead/{category}")
  public List<Order> getBulkheadOrdersByCategory(@PathVariable String category){
    return orderRepository.findByCategory(category);
  }
  @GetMapping("/threadPoolBulkhead/{category}")
  public List<Order> getThreadPoolBulkheadOrdersByCategory(@PathVariable String category){
    return orderRepository.findByCategory(category);
  }

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

}
