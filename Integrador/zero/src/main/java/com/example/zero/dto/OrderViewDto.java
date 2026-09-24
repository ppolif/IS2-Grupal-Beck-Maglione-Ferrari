package com.example.zero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderViewDto {
    private String id;
    private String orderNumber;
    private Long numeroFactura;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAvatar;
    private String productSummary;
    private String categoryName;
    private double totalAmount;
    private double subtotal;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String shippingAddress;
    private String shippingCity;
    private String shippingZip;

    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
    }
}

