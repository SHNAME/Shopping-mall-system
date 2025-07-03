package com.example.demo.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Product
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String image_url;

    @Column(nullable = false)
    private int stock;

    @ManyToOne
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;


    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductImage> subImageList= new ArrayList<>();


    public void addImage(ProductImage image){
        subImageList.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image){
        subImageList.remove(image);
        image.setProduct(null);
    }






}
