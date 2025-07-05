package com.example.demo.domain.embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingInfo {
    String name;
    String phoneNumber;
    String address_line;
    public ShippingInfo() {
    }
}
