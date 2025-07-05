package com.example.demo.domain;

import com.example.demo.domain.embeddable.ShippingInfo;
import com.example.demo.en.OrderStatus;
import com.example.demo.en.PaymentMethod;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class UserOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private int total_price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime order_date;
    
    //무통장 입금의 경우 주문 생성 시 결제를 할 수 없음 -> 나중에 업데이트
    private LocalDateTime paid_at;

    //사용자 요청 메세지(옵션)
    private String order_message;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @Setter
    private UserData user;

    @Embedded
    @Column(nullable = false)
    private ShippingInfo shippingInfo;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> orderItemList = new ArrayList<>();

    public void addOrderItem(OrderItem item){
        orderItemList.add(item);
        item.setOrder(this);
    }










}
