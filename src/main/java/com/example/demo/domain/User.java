package com.example.demo.domain;

import com.example.demo.en.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Role role;

    //1:1 관계 Mapping User가 주인으로, User 데이터가 저장, 삭제, 갱신할 때 Address도 같은 연산 처리
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Address address;

    //사용자가 삭제된다고 해서 주문이 삭제는 x, 주문은 서버의 중요 Data
    @OneToMany(mappedBy = "user")
    private List<Order> orderList = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name ="cart_id",nullable = false)
    //장바구니 조회에 사용
    private Cart cart;


    public void setAddress(Address address){
        this.address = address;
        address.setUser(this);
    }

    public void addOrder(Order order){
        orderList.add(order);
        order.setUser(this);
    }




}
