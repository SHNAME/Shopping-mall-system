package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int price_snapshot;

    @ManyToOne
    @JoinColumn(name = "order_id",nullable = false)
    @Setter
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;



}
