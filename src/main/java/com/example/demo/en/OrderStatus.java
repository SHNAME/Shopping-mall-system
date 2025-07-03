package com.example.demo.en;

public enum OrderStatus {
    PENDING("결제대기"),
    PAID("결제완료"),
    PROCESSING("상품 준비 중"),
    SHIPPED("배송 시작"),
    DELIVERED("수령 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description){
        this.description = description;
    }
}
