package com.example.demo.en;

import lombok.Getter;

public enum PaymentMethod {
    CARD("카드결제"),
    BANK_TRANSFER("무통장입금"),
    NAVER_PAY("네이버페이");

    @Getter
    private  final String description;

    PaymentMethod(String description){
        this.description = description;
    }
}
