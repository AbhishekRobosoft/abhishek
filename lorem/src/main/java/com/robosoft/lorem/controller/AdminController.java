package com.robosoft.lorem.controller;
import com.cloudinary.utils.ObjectUtils;
import com.robosoft.lorem.model.Brand;
import com.robosoft.lorem.service.AdminService;
import com.robosoft.lorem.service.CloudinaryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;

@RestController
public class AdminController
{
    @Autowired
    AdminService adminService;

    @Autowired
    CloudinaryConfig cloudinary;

    @PostMapping("/addBrand")
    public ResponseEntity<?> addBrand(@ModelAttribute Brand brand)
    {
        try
        {
            Map uploadResult = cloudinary.upload(brand.getLogo().getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            brand.setLogoLink(uploadResult.get("url").toString());
            Map uploadResult2 = cloudinary.upload(brand.getProfilePic().getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
            brand.setProfileLink(uploadResult2.get("url").toString());
            adminService.addBrand(brand);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }

}
