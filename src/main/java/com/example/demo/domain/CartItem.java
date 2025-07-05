package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean isSelected;

    @ManyToOne
    @JoinColumn(name="cart_id",nullable = false)
    @Setter
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;



}
