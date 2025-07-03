package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Setter;

import java.util.ArrayList;

@Entity
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(nullable = false)
    private String image_url;


    @ManyToOne
    @JoinColumn(name = "product_id",nullable = false)
    @Setter
    private Product product;



}
