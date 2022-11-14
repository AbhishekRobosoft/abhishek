package com.robosoft.lorem.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class Photo
{
    private MultipartFile photo;
    private int reviewId;
}
