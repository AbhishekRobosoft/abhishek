package com.robosoft.lorem.controller;

import com.robosoft.lorem.model.Brand;
import com.robosoft.lorem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController
{
    @Autowired
    AdminService adminService;

    @PostMapping("/addBrand")
    public String addBrand(@ModelAttribute Brand brand)
    {
        return adminService.addBrand(brand);
    }


}
