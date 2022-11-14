package com.robosoft.lorem.controller;
import com.robosoft.lorem.model.*;
import com.robosoft.lorem.response.BrandList;
import com.robosoft.lorem.response.OrderDetails;
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
    public String addToFav(@RequestBody FavTable favTable)
    {
        return userServiceImpl.addToFavourite(favTable);
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
    public String addReview(@ModelAttribute ReviewInfo reviewInfo)
    {
        return userServiceImpl.addReview(reviewInfo);
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
    public OrderDetails getOrderDetails(@RequestBody com.robosoft.lorem.model.User user)
    {
        return userServiceImpl.getOrderDetails(user);
    }

    @PostMapping("/addCard")
    public String addCard(@RequestBody Card card)
    {
        return userServiceImpl.addCard(card);
    }

    @GetMapping("/viewCards")
    public ResponseEntity<?> viewCards(@RequestBody User user)
    {
        try
        {
            List<Card> viewCards=userServiceImpl.viewCards(user);
            if(viewCards.size()==0)
            {
                return new ResponseEntity<>("No Cards Saved Please Add Some Cards",HttpStatus.FORBIDDEN);
            }
           return new ResponseEntity<>(viewCards,HttpStatus.ACCEPTED);
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


}
