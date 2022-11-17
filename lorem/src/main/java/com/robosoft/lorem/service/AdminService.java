package com.robosoft.lorem.service;
import com.robosoft.lorem.model.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class AdminService
{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String addBrand(Brand brand)
    {
        jdbcTemplate.update("insert into brand(brandName, description, logo, profilePic, brandOrigin) values (?,?,?,?,?)",brand.getBrandName(),brand.getDescription(),brand.getLogoLink(),brand.getProfileLink(),brand.getBrandOrigin());
        return "Added Successfully";
    }
}









