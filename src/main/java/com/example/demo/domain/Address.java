package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Setter;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;
    
    //01012345678 형식
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address_line;

    @OneToOne
    @JoinColumn(name ="user_id",nullable = false,unique = true)
    @Setter
    private  User user;

}
