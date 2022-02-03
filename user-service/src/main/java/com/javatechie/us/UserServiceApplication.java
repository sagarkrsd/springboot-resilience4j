package com.javatechie.us;

import com.javatechie.us.dto.OrderDTO;
import com.javatechie.us.dto.ProductDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.apache.http.ssl.TrustStrategy;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import java.security.cert.X509Certificate;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import org.apache.http.client.config.RequestConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
@RequestMapping("/user-service")
public class UserServiceApplication {

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    public static final String USER_SERVICE="userService";

    @Value("${CATALOG_SERVICE_URL}")
    private String BASEURL;

    @Value("${PRODUCT_SERVICE_URL}")
    private String PRODUCT_URL;

//    @Value("${TIMEOUT}")
//    private int Timeout;

    @GetMapping("/displayOrders")
    @CircuitBreaker(name =USER_SERVICE,fallbackMethod = "getAllAvailableProducts")
    public List<OrderDTO> displayOrders(@RequestParam("category") String category) {
        System.out.println("***** /displayOrders *****: executed API call successfully at " + new Date());
        String url = category == null ? BASEURL : BASEURL + "/" + category;
        try {
            return restTemplate.getForObject(url, ArrayList.class);
        } catch (HttpServerErrorException e) {
            System.err.println("Error occurred while fetching orders: " + e.getRawStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            //System.err.println("An unexpected error occurred: " + e.getMessage());
            throw e;
        }
    }

    @Async
    @GetMapping("/displayProducts")
    @Bulkhead(name = USER_SERVICE,type= Bulkhead.Type.THREADPOOL,fallbackMethod = "threadPoolBulkHeadFallbackProducts")
    public CompletableFuture<List<ProductDTO>> displayProducts(@RequestParam("category") String category) {
        System.out.println("***** /displayProducts *****: executed API call successfully at "+ new Date());
        String url = category == null ? PRODUCT_URL : PRODUCT_URL + "/" + category;
        return CompletableFuture.completedFuture(restTemplate.getForObject(url, ArrayList.class));
    }

    @GetMapping("/displayProductsBulkhead")
    @Bulkhead(name = USER_SERVICE,type= Bulkhead.Type.SEMAPHORE,fallbackMethod = "bulkHeadFallbackProducts")
    public List<ProductDTO> displayProductsBulkhead(@RequestParam("category") String category) {
        System.out.println("***** /displayProductsBulkhead *****: executed API call successfully at " + new Date());
        String url = category == null ? PRODUCT_URL : PRODUCT_URL + "/" + category;
        return restTemplate.getForObject(url, ArrayList.class);
    }

    @GetMapping("/displayOrdersRateLimit")
    @RateLimiter(name = USER_SERVICE,fallbackMethod = "rateLimiterFallback")
    public List<OrderDTO> displayOrdersRateLimiter(@RequestParam("category") String category) {
        String url = category == null ? BASEURL : BASEURL + "/" + category;
        System.out.println("***** /displayOrdersRateLimiter *****: executed API call successfully at " + new Date());
        return restTemplate.getForObject(url, ArrayList.class);
    }

    public List<OrderDTO> getAllAvailableProducts(Exception e){
        System.out.println("***** /displayOrders fallback response returned at " + new Date() + " *****: " + e);
        return Stream.of(
            new OrderDTO(119, "LED TV", "electronics", 1, 45000),
            new OrderDTO(345, "Headset", "electronics", 2, 10000),
            new OrderDTO(475, "Sound bar", "electronics", 1, 13000)
        ).collect(Collectors.toList());
    }

    public CompletableFuture<List<ProductDTO>> threadPoolBulkHeadFallbackProducts(Exception e)
    {
        System.out.println("***** /displayProducts fallback response returned at " + new Date() + " *****: " + e);
        return  CompletableFuture.completedFuture(Stream.of(
            new ProductDTO(201,"Acoustic Guitar","Full-size acoustic guitar with mahogany body",
                "Music", "grey", 10000, 4),
            new ProductDTO(202,"Smart LED Bulb","Energy-saving smart LED bulb with app control",
                "Home Improvement", "white", 300, 5)
        ).collect(Collectors.toList()));
    }

    public List<ProductDTO> bulkHeadFallbackProducts(Exception e)
    {
        System.out.println("***** /displayProductsBulkhead fallback response returned at " + new Date() + " *****: " + e);
        return  Stream.of(
            new ProductDTO(201,"Acoustic Guitar","Full-size acoustic guitar with mahogany body",
                "Music", "grey", 10000, 4),
            new ProductDTO(202,"Smart LED Bulb","Energy-saving smart LED bulb with app control",
                "Home Improvement", "white", 300, 5)
        ).collect(Collectors.toList());
    }

     public List<OrderDTO> rateLimiterFallback(Exception e)
        {
            System.out.println("***** /displayOrdersRateLimit fallback response returned at " + new Date() + " *****: " + e);
            return  Stream.of(
                new OrderDTO(119, "LED TV", "electronics", 1, 45000),
                new OrderDTO(345, "Headset", "electronics", 2, 10000),
                new OrderDTO(475, "Sound bar", "electronics", 1, 13000)
            ).collect(Collectors.toList());
        }


    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .setDefaultRequestConfig(RequestConfig.custom()
//                        .setSocketTimeout(Timeout) // Socket timeout in milliseconds
//                        .setConnectTimeout(Timeout) // Connection timeout in milliseconds
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

}
