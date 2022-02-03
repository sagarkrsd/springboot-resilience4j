package com.javatechie.os.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PRODUCTS_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String description;
    private String category;
    private String color;
    private double price;
    private double rating;

    public Product(String name, String description, String category, String color, double price, double rating) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.color = color;
        this.price = price;
        this.rating = rating;
    }
}
