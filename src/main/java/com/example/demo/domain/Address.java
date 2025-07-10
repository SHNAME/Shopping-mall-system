package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
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
    private UserData user;

    public Address(String name,String phoneNumber,String address_line){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address_line = address_line;
    }

    public Address() {

    }

}
