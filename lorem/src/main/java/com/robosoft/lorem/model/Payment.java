package com.robosoft.lorem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Payment
{
    private String paymentType;
    private int paymentId;
    private int userId;
    private int orderId;
    private float amount;
    private float discount;
    private float taxAmount;
    private float grandTotal;
    private String promoCode;
    private String cvv;
    private String cardNo;
    private String paymentStatus;
    private String orderStatus;
}
