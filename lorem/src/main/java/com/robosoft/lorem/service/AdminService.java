package com.robosoft.lorem.service;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.robosoft.lorem.model.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class AdminService
{


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String addBrand(Brand brand)
    {
        try
        {
            String fileName = brand.getLogo().getOriginalFilename();
            String fileName1= brand.getProfilePic().getOriginalFilename();
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));  // to generated random string values for file name.
            fileName1 = UUID.randomUUID().toString().concat(this.getExtension(fileName1));
            File file = this.convertToFile(brand.getLogo(), fileName);
            File file1=this.convertToFile(brand.getProfilePic(), fileName1);// to convert multipartFile to File
            String TEMP_URL = this.uploadFile(file, fileName);
            String URl=this.uploadFile(file1,fileName1);// to get uploaded file link
            file.delete();
            file1.delete();// to delete the copy of uploaded file stored in the project folder
            jdbcTemplate.update("insert into brand (brandName, description, logo, profilePic) values(?,?,?,?)",brand.getBrandName(),brand.getDescription(),TEMP_URL,URl);
            return "Successfully Added";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Failed to Upload";
        }
    }

    public String uploadFile(File file, String fileName) throws IOException
    {
        BlobId blobId = BlobId.of("image-3edad.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\Abhishek N\\Downloads\\image.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return String.format("https://firebasestorage.googleapis.com/v0/b/image-3edad.appspot.com/o/%s?alt=media&token=", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    public File convertToFile(MultipartFile multipartFile, String fileName) throws IOException
    {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile))
        {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    public String getExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf("."));
    }







}
