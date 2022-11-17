package com.robosoft.lorem.service;
import com.robosoft.lorem.model.*;
import com.robosoft.lorem.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDate;
import java.util.*;

@Service
public class UserService implements UserDetailsService
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    AdminService adminService;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    int lowerLimit = 0;
    int upperLimit = 1;
    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException
    {
        String email = jdbcTemplate.queryForObject("select emailId from user where emailId=?", String.class, new Object[]{emailId});
        String password = jdbcTemplate.queryForObject("select password from user where emailId=?", String.class, new Object[]{emailId});
        return new User(email, password, new ArrayList<>());
    }

    public String getUserNameFromToken()
    {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
        {
            username = ((UserDetails) principal).getUsername();
        }
        else
        {
            username = principal.toString();
        }
        return username;
    }

    public String addToFavourite(FavTable favTable)
    {
        try
        {
            String email = getUserNameFromToken();
            int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
            jdbcTemplate.update("insert into favTable values(?,?)", id, favTable.getBrandId());
            return "added to favourites";
        }
        catch (Exception e)
        {
            return "Something Went Wrong";
        }
    }

    public Map<Integer, List<BrandList>> viewPopularBrands()
    {

        Map<Integer, List<BrandList>> popular = new HashMap<>();
        try
        {
            int brandNo = jdbcTemplate.queryForObject("select brandId from favTable group by brandId limit ?,?", Integer.class, new Object[]{lowerLimit, upperLimit});
            List<BrandList> brands = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand where brandId=?", new BeanPropertyRowMapper<>(BrandList.class), brandNo);
            lowerLimit = lowerLimit + 1;
            popular.put(brands.size(), brands);
            return popular;
        }
        catch (EmptyResultDataAccessException e)
        {
            lowerLimit = lowerLimit - 1;
            int brandNo = jdbcTemplate.queryForObject("select brandId from favTable group by brandId limit ?,?", Integer.class, new Object[]{lowerLimit, upperLimit});
            List<BrandList> brands = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand where brandId=?", new BeanPropertyRowMapper<>(BrandList.class), brandNo);
            popular.put(brands.size(), brands);
            return popular;
        }
    }

    public Map<Integer, List<BrandList>> viewAllBrands()
    {
        Map<Integer, List<BrandList>> theThings = new HashMap<>();
        List<BrandList> brandLists = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand", new BeanPropertyRowMapper<>(BrandList.class));
        theThings.put(brandLists.size(), brandLists);
        return theThings;
    }

    public String addReview(ReviewInfo reviewInfo)
    {
        try
        {
            String email = getUserNameFromToken();
            int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
            int userId = jdbcTemplate.queryForObject("select userId from orders where userId=? group by userId", Integer.class, new Object[]{id});
            int restaurantId = jdbcTemplate.queryForObject("select restaurantId from orders where userId=? group by restaurantId", Integer.class, new Object[]{userId});
            if (restaurantId == reviewInfo.getRestaurantId() || id==userId)
            {
                String query = "insert into review (userId, restaurantId, description, localDate, foodRating, serviceRating) values(?,?,?,?,?,?)";
                jdbcTemplate.update(query, reviewInfo.getUserId(), reviewInfo.getRestaurantId(), reviewInfo.getDescription(), LocalDate.now(), reviewInfo.getFoodRating(), reviewInfo.getServiceRating());
                int reviewId = jdbcTemplate.queryForObject("select max(reviewId) from review where userId=?", Integer.class, new Object[]{reviewInfo.getUserId()});
                ReviewInfo reviewInfo1 = jdbcTemplate.queryForObject("select foodRating, serviceRating from review where reviewId=?", new BeanPropertyRowMapper<>(ReviewInfo.class), reviewId);
                jdbcTemplate.update("update review set averageRating=? where reviewId=?", (reviewInfo1.getFoodRating() + reviewInfo1.getServiceRating()) / 2, reviewId);
                try
                {
                    if (reviewInfo.getPhotoLinks()!=null)
                    {
                        for(int i=0;i<reviewInfo.getPhotoLinks().size();i++)
                        {
                            jdbcTemplate.update("insert into photo (photoPic, reviewId) values(?,?)",reviewInfo.getPhotoLinks().get(i),reviewId);
                        }
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Review Added Without photo";
                }
            }
            else
            {
                return "You Cant give Review to this Restaurant";
            }
            return "Review Added With Photo";
        }
        catch (Exception e)
        {
            return "Failed to add review";
        }

    }

    public Map<Integer, Object> viewReviews(Restaurant restaurant)
    {
        Map<Integer, Object> reviews = new HashMap<>();
        try
        {
            String query = "select user.userId, user.firstName, user.lastName, user.profilePic, review.reviewId, review.description, review.averageRating, review.likeCount, review.localDate from user inner join review on user.userId=review.userId where review.restaurantId=?";
            List<ReviewPageResponse> reviewPageResponses = new ArrayList<ReviewPageResponse>();
            jdbcTemplate.query(query, (rs, rowNum) ->
            {
                ReviewPageResponse reviewPageResponse = new ReviewPageResponse();
                reviewPageResponse.setUserId(rs.getInt("user.userId"));
                reviewPageResponse.setFirstName(rs.getString("user.firstName"));
                reviewPageResponse.setLastName(rs.getString("user.lastName"));
                reviewPageResponse.setProfilePic(rs.getString("user.profilePic"));
                reviewPageResponse.setReviewId(rs.getInt("review.reviewId"));
                reviewPageResponse.setDescription(rs.getString("review.description"));
                reviewPageResponse.setAverageRating(rs.getInt("review.averageRating"));
                reviewPageResponse.setLikeCount(rs.getInt("review.likeCount"));
                reviewPageResponse.setDate(rs.getDate("review.localDate"));
                reviewPageResponse.setPhoto(getReviewPhotos(rs.getInt("review.reviewId")));
                reviewPageResponse.setReviewCount(giveReviewCount(rs.getInt("user.userId")));
                reviewPageResponse.setRatingCount(giveRatingCount(rs.getInt("user.userId")));
                reviewPageResponses.add(reviewPageResponse);
                reviews.put(reviewPageResponses.size(), reviewPageResponse);
                return reviewPageResponse;
            }, restaurant.getRestaurantId());
            return reviews;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public List<String> getReviewPhotos(int reviewId)
    {
        return jdbcTemplate.queryForList("select photoPic from photo where reviewId=" + reviewId, String.class);
    }

    public int giveReviewCount(int userId)
    {
        return jdbcTemplate.queryForObject("select count(userId) from review where userId=?", Integer.class, new Object[]{userId});
    }

    public int giveRatingCount(int userId)
    {
        int totalFoodRating = jdbcTemplate.queryForObject("select sum(foodRating) from review where userId=?", Integer.class, new Object[]{userId});
        int totalServiceRating = jdbcTemplate.queryForObject("select sum(serviceRating) from review where userId=?", Integer.class, new Object[]{userId});
        int totalRating = totalFoodRating + totalServiceRating;
        return totalRating;
    }

    public OrderDetails getOrderDetails(Orders orders)
    {
        String query = "select orders.orderId, orders.cartId, cart.scheduledDate, cart.scheduledTime, cart.restaurantId, restaurant.restaurantName, address.addressDesc from cart inner join orders on orders.cartId=cart.cartId inner join restaurant on orders.restaurantId=restaurant.restaurantId inner join address on orders.addressId=address.addressId where orders.userId=? and orders.orderId=?";
        return jdbcTemplate.queryForObject(query, (rs, rowNum) ->
        {
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrderId(rs.getInt("orders.orderId"));
            orderDetails.setCartId(rs.getInt("orders.cartId"));
            orderDetails.setScheduleDate(rs.getString("cart.scheduledDate"));
            orderDetails.setScheduleTime(rs.getString("cart.scheduledTime"));
            orderDetails.setRestaurantId(rs.getInt("cart.restaurantId"));
            orderDetails.setRestaurantName(rs.getString("restaurant.restaurantName"));
            orderDetails.setDeliveryAddress(rs.getString("address.addressDesc"));
            orderDetails.setDishInfoList(giveDishDetails(rs.getInt("cart.restaurantId"), rs.getInt("orders.cartId")));
            orderDetails.setAmountDetails(provideAmountDetails(rs.getInt("orders.orderId")));
            return orderDetails;
        }, orders.getUserId(), orders.getOrderId());
    }

    public List<DishInfo> giveDishDetails(int restaurantId, int cartId)
    {
        String query = "select item.count, item.addOnCount, item.dishId, menu.price , dish.dishName, dish.veg from item inner join menu on item.dishId=menu.dishId inner join dish on menu.dishId=dish.dishId where menu.restaurantId=? and item.cartId=?";
        List<DishInfo> dishInfo1 = jdbcTemplate.query(query, (rs, rowNum) ->
        {
            DishInfo dishInfo = new DishInfo();
            dishInfo.setDishId(rs.getInt("item.dishId"));
            dishInfo.setCount(rs.getInt("item.count"));
            dishInfo.setAddOnCount(rs.getInt("item.addOnCount"));
            dishInfo.setPrice(rs.getInt("menu.price"));
            dishInfo.setDishName(rs.getString("dish.dishName"));
            dishInfo.setVeg(rs.getBoolean("dish.veg"));
            dishInfo.setAddonInfoList(giveAddOnInfoForDish(restaurantId, dishInfo));
            return dishInfo;
        }, restaurantId, cartId);
        return dishInfo1;
    }

    public List<AddonInfo> giveAddOnInfoForDish(int restaurantId, DishInfo dishInfo)
    {
        String addOn_Query = "select addOn.addOn, addOn.price from addOn inner join addOnMapping on addOn.addOnId=addOnMapping.addOnId where addOnMapping.dishId=? and addOnMapping.restaurantId=?";
        if (dishInfo.getAddOnCount() != 0)
        {
            List<AddonInfo> addonInfoList = jdbcTemplate.query(addOn_Query, (rs, rowNum) ->
            {
                AddonInfo addonInfo = new AddonInfo();
                addonInfo.setAddOn(rs.getString("addOn.addOn"));
                addonInfo.setPrice(rs.getInt("addOn.price"));
                return addonInfo;
            }, dishInfo.getDishId(), restaurantId);
            return addonInfoList;
        }
        else
        {
            return null;
        }
    }

    public AmountDetails provideAmountDetails(int orderId)
    {
        AmountDetails amountDetails = new AmountDetails();
        try
        {
            String query = "select cardNo from payment where orderId=?";
            jdbcTemplate.queryForObject(query, String.class, new Object[]{orderId});
            jdbcTemplate.queryForObject("select amount, taxAmount, discount, grandTotal from payment where orderId=?", (rs, rowNum) ->
            {
                amountDetails.setTotalAmount(rs.getFloat("amount"));
                amountDetails.setTaxAmount(rs.getFloat("taxAmount"));
                amountDetails.setDiscount(rs.getFloat("discount"));
                amountDetails.setAmountPaid(rs.getFloat("grandTotal"));
                amountDetails.setPaymentType("Credit/Debit card");
                return amountDetails;
            }, orderId);
            return amountDetails;
        }
        catch (Exception e)
        {
            jdbcTemplate.queryForObject("select amount, taxAmount, discount, grandTotal from payment where orderId=?", (rs, rowNum) ->
            {
                amountDetails.setTotalAmount(rs.getFloat("amount"));
                amountDetails.setTaxAmount(rs.getFloat("taxAmount"));
                amountDetails.setDiscount(rs.getFloat("discount"));
                amountDetails.setAmountPaid(rs.getFloat("grandTotal"));
                amountDetails.setPaymentType("Cash");
                return amountDetails;
            }, orderId);
            return amountDetails;
        }
    }

    public String addCard(Card card)
    {
        try
        {
            String hash=passwordEncoder.encode(card.getCvv());
            jdbcTemplate.update("insert into card (cardNo, cardName, expiryDate, cvv, userId) values (?,?,?,?,?)",card.getCardNo(),card.getCardName(),card.getExpiryDate(),hash,card.getUserId());
            return "Card Added Successfully";
        }
        catch (Exception e)
        {
            return "Failed to Add Card";
        }

    }

    public List <Card> viewCards(com.robosoft.lorem.model.User user)
    {
        return jdbcTemplate.query("select cardNo, cardName, expiryDate from card where userId=?", new BeanPropertyRowMapper<>(Card.class),user.getUserId());
    }

    public boolean editCard(Card card)
    {
        try
        {
            String expiry = jdbcTemplate.queryForObject("select expiryDate from card where cardNo=?", String.class, new Object[]{card.getCardNo()});
            expiry = expiry.substring(3);
            int date = LocalDate.now().getYear();
            String year = Integer.toString(date).substring(2);
            int cardYear = Integer.parseInt(expiry);
            int currentYear = Integer.parseInt(year);
            if (cardYear <= currentYear)
            {
                jdbcTemplate.update("update card set expiryDate=? where cardNo=?", card.getExpiryDate(), card.getCardNo());
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public String makeCardPrimary(Card card)
    {
        jdbcTemplate.update("update card set cardType=1 where cardNo= ? and userId=?",card.getCardNo(),card.getUserId());
        jdbcTemplate.update("update card set cardType=0 where cardNo!=? and userId=?",card.getCardNo(),card.getUserId());
        return card.getCardNo()+"selected as primary";
    }

    public String deleteCard(Card card)
    {
        jdbcTemplate.update("update card set cardDeleted=1 where cardNo=? and userId=?",card.getCardNo(),card.getUserId());
        return card.getCardNo()+" Removed successfully";
    }

    public List<Card> makeOrder(Orders order)
    {
        String email = getUserNameFromToken();
        int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
        jdbcTemplate.update("insert into orders (orderType, addressId, contactName, contactNumber, deliveryInstructions, cartId, userId) values(?,?,?,?,?,?,?)",order.getOrderType(),order.getAddressId(),order.getContactName(),order.getContactNo(),order.getDeliveryInstructions(),order.getCartId(),id);
        int restaurantId=jdbcTemplate.queryForObject("select restaurantId from cart where cartId=?", Integer.class, new Object[]{order.getCartId()});
        jdbcTemplate.update("update order set restaurantId=? where cartId=?",restaurantId,order.getOrderId());
        return  jdbcTemplate.query("select cardNo, cardName, expiryDate from card where userId=?", new BeanPropertyRowMapper<>(Card.class),order.getUserId());
    }

    public String  makePayment(Payment payment)
    {
        String email = getUserNameFromToken();
        int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
        try
        {
            String cvv = jdbcTemplate.queryForObject("select cvv from card where cardNo=?", String.class, new Object[]{payment.getCardNo()});
            System.out.println(cvv);
            System.out.println(payment.getCvv());
            if (passwordEncoder.matches(payment.getCvv(), cvv))
            {
                jdbcTemplate.update("insert into payment (userId, orderId, amount, promoCode, cardNo, taxAmount, discount, grandTotal) values(?,?,?,?,?,?,?,?)", id, payment.getOrderId(), payment.getAmount(), payment.getPromoCode(), payment.getCardNo(), payment.getTaxAmount(), payment.getDiscount(), payment.getGrandTotal());
                jdbcTemplate.update("update payment set paymentStatus=? where orderId=?"," Paid",payment.getOrderId());
                payment.setOrderStatus("orderPlaced");
                jdbcTemplate.update("update orders set orderStatus=? where orderId=?", payment.getOrderStatus(), payment.getOrderId());
                int cartId=jdbcTemplate.queryForObject("select cartId from orders where orderId=?",Integer.class, new Object[]{payment.getOrderId()});
                jdbcTemplate.update("update cart set cartDeleted=1 where cartId=?",cartId);
                return payment.getOrderStatus();
            }
            else
            {
                return "Payment failed";
            }
        }
        catch (Exception e)
        {
            jdbcTemplate.update("insert into payment (userId, orderId, amount, promoCode, taxAmount, discount, grandTotal) values(?,?,?,?,?,?,?)", id, payment.getOrderId(), payment.getAmount(), payment.getPromoCode(), payment.getTaxAmount(), payment.getDiscount(), payment.getGrandTotal());
            jdbcTemplate.update("update payment set paymentStatus=? where orderId=?","Not Paid",payment.getOrderId());
            payment.setOrderStatus("orderPlaced");
            jdbcTemplate.update("update orders set orderStatus=? where orderId=?", payment.getOrderStatus(), payment.getOrderId());
            int cartId=jdbcTemplate.queryForObject("select cartId from orders where orderId=?",Integer.class, new Object[]{payment.getOrderId()});
            jdbcTemplate.update("update cart set cartDeleted=1 where cartId=?",cartId);
            return payment.getOrderStatus();
        }
    }

    public String giveFeedback(FeedBack feedBack)
    {
        String email = getUserNameFromToken();
        int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
        jdbcTemplate.update("insert into feedback (userId, role, feedBackDescription, userName, entityName, contactEmail, contactNo, city, area, category) values (?,?,?,?,?,?,?,?,?,?)",id,feedBack.getRole(),feedBack.getMessage(),feedBack.getName(),feedBack.getEntityName(),feedBack.getContactEmailId(),feedBack.getContactMobileNumber(),feedBack.getEntityCity(),feedBack.getEntityArea(),feedBack.getCategoryType());
        return "Thank You for your feedback";
    }



}


