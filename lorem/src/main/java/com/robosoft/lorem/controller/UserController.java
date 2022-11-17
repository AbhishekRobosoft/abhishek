package com.robosoft.lorem.controller;
import com.cloudinary.utils.ObjectUtils;
import com.robosoft.lorem.model.*;
import com.robosoft.lorem.response.BrandList;
import com.robosoft.lorem.response.OrderDetails;
import com.robosoft.lorem.service.CloudinaryConfig;
import com.robosoft.lorem.service.UserService;
import com.robosoft.lorem.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class UserController
{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userServiceImpl;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    CloudinaryConfig cloudinary;


    @PostMapping("/authenticate")
    public JWTResponse authenticate(@RequestBody JWTRequest jwtRequest) throws Exception
    {

        try
        {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            jwtRequest.getEmailId(),
                            jwtRequest.getPassword()
                    )
            );
        }
        catch (BadCredentialsException e)
        {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = userServiceImpl.loadUserByUsername(jwtRequest.getEmailId());

        final String token = jwtUtility.generateToken(userDetails);

        return  new JWTResponse(token);
    }

    @PutMapping("/likeBrand")
    public ResponseEntity<String> addToFav(@RequestBody FavTable favTable)
    {
        try
        {
            boolean check=userServiceImpl.addToFavourite(favTable);
            if(check==true)
            {
                return new ResponseEntity("Added to favorites",HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity("Failed to add favorites",HttpStatus.BAD_GATEWAY);
            }
        }
        catch (Exception e)
        {
            return new ResponseEntity("Failed to add favorites",HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/viewPopularBrands")
    public ResponseEntity<?>listPopularBrands()
    {
        try
        {
            Map<Integer, List<BrandList>> brandLists=userServiceImpl.viewPopularBrands();
            if(brandLists.size()==0)
            {
                return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(brandLists,HttpStatus.OK);
        }
        catch (Exception exception)
        {
            return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/viewAllBrands")
    public ResponseEntity<?> viewAllPopularBrands()
    {
        try
        {
            Map<Integer, List<BrandList>> brandLists=userServiceImpl.viewAllBrands();
            if(brandLists.size()==0)
            {
                return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(brandLists,HttpStatus.OK);
        }
        catch (Exception exception)
        {
            return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/review")
    public ResponseEntity<String> addReview(@ModelAttribute ReviewInfo reviewInfo)
    {
        try
        {
            try
            {
                List<String> photoLinks = new ArrayList<String>();
                for (int i = 0; i < reviewInfo.getMultipartFileList().size(); i++)
                {
                    Map uploadResult = cloudinary.upload(reviewInfo.getMultipartFileList().get(i).getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                    photoLinks.add(uploadResult.get("url").toString());
                }
                reviewInfo.setPhotoLinks(photoLinks);
                userServiceImpl.addReview(reviewInfo);
            }
            catch (Exception e)
            {
                userServiceImpl.addReview(reviewInfo);
            }
            return new ResponseEntity(HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity("Failed to add review",HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/getReviews")
    public ResponseEntity<?> getReviews(@RequestBody Restaurant restaurant)
    {
        try
        {
            Map<Integer,Object> reviewPageResponseList= userServiceImpl.viewReviews(restaurant);
            if(reviewPageResponseList.size()==0)
            {
                return new ResponseEntity<>("No Reviews to this restaurant",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(reviewPageResponseList, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("No Reviews To this restaurant",HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/getOrderDetails")
    public OrderDetails getOrderDetails(@RequestBody Orders orders)
    {
        return userServiceImpl.getOrderDetails(orders);
    }

    @PostMapping("/addCard")
    public ResponseEntity<String> addCard(@RequestBody Card card)
    {
        try
        {
            boolean message=userServiceImpl.addCard(card);
            if(message==true)
            {
                return new ResponseEntity("Card added successfully", HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity("Failed to add card", HttpStatus.BAD_GATEWAY);
            }
        }
        catch (Exception e)
        {
            return new ResponseEntity("Failed to add card", HttpStatus.BAD_GATEWAY);
        }

    }

    @GetMapping("/viewCards")
    public ResponseEntity<?> viewCards(@RequestBody User user)
    {
        try
        {
            Map<Integer, List<Card>> cardLists=userServiceImpl.viewCards(user);
            if(cardLists.size()==0)
            {
                return new ResponseEntity<>("No Cards Saved Please Add Some Cards",HttpStatus.FORBIDDEN);
            }
           return new ResponseEntity<>(cardLists,HttpStatus.ACCEPTED);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("No Cards Saved Please Add Some Cards",HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/editCard")
    public ResponseEntity<String> editCard(@RequestBody Card card)
    {
        try
        {
            if(this.userServiceImpl.editCard(card)==true)
            {
                return ResponseEntity.ok().body("Card Updated Successfully");
            }
            else
            {
                return ResponseEntity.ok().body("You cant edit this card");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @PutMapping("/makeCardPrimary")
    public ResponseEntity<String> makeCardPrimary(@RequestBody Card card)
    {
        try
        {
            this.userServiceImpl.makeCardPrimary(card);
            return ResponseEntity.ok().body(card.getCardNo()+" Selected as primary");
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Something went wrong");
        }
    }

    @PutMapping("/deleteCard")
    public ResponseEntity<String> deleteCard(@RequestBody Card card)
    {
        try
        {
            this.userServiceImpl.deleteCard(card);
            return ResponseEntity.ok().body(card.getCardNo()+" Removed successfully");
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Something went wrong");
        }
    }

    @PutMapping("/makePayment")
    public ResponseEntity<String> makePayment(@RequestBody Payment payment)
    {
        try
        {
            String message = userServiceImpl.makePayment(payment);
            return new ResponseEntity(message, HttpStatus.OK);
        }
        catch (Exception e)
        {
            String message=userServiceImpl.makePayment(payment);
            return new ResponseEntity(message,HttpStatus.BAD_GATEWAY);
        }
    }

    @PutMapping("/giveFeedback")
    public ResponseEntity<String> giveFeedback(@RequestBody FeedBack feedBack)
    {
      try
      {
          if(userServiceImpl.giveFeedback(feedBack)==true)
          {
              return new ResponseEntity("Thank you for your feedback", HttpStatus.OK);
          }
          else
          {
              return new ResponseEntity("Something went wrong",HttpStatus.BAD_GATEWAY);
          }
      }
      catch (Exception e)
      {
          return new ResponseEntity("Something went wrong",HttpStatus.BAD_GATEWAY);
      }

    }
}
