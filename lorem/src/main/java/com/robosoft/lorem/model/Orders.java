package com.robosoft.lorem.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Orders
{
    private int orderId;
    private String orderStatus;
    private String orderType;
    private int userId;
    private String contactName;
    private String contactNo;
    private int addressId;
    private int cartId;
    private int restaurantId;
    private String deliveryInstructions;
}
