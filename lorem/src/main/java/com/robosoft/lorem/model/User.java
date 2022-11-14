package com.robosoft.lorem.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class User
{
    private int userId;
    private String firstName;
    private String lastName;
    private String emailId;
    private String mobileNo;
    private String profilePic;
    private int creditScore;
    private String role;
    private String password;
}
