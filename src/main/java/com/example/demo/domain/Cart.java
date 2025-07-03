package com.example.demo.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @OneToMany(mappedBy = "cart",orphanRemoval = true,cascade = CascadeType.ALL)
    private List<CartItem> cartItemList = new ArrayList<>();

    public void addItem(CartItem item){
        cartItemList.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item){
        cartItemList.remove(item);
        item.setCart(null);
    }


}
