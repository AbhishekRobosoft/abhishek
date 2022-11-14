package com.robosoft.lorem.response;
import lombok.Data;

@Data
public class AmountDetails
{
    private int AmountPaid;
    private int tax;
    private String paymentType;
}
