package com.driver.services;


import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.model.WebSeries;
import com.driver.repository.UserRepository;
import com.driver.repository.WebSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebSeriesRepository webSeriesRepository;


    public Integer addUser(User user){

        //Jut simply add the user to the Db and return the userId returned by the repository
        if (user.getSubscription() != null) {
            // Ensure the subscription is linked to the user
            user.getSubscription().setUser(user);
        }
        userRepository.save(user);
        return user.getId();
    }

    public Integer getAvailableCountOfWebSeriesViewable(Integer userId){

        //Return the count of all webSeries that a user can watch based on his ageLimit and subscriptionType
        //Hint: Take out all the Webseries from the WebRepository
        List <WebSeries> webSeriesList=webSeriesRepository.findAll();
        User user=userRepository.findById(userId).orElse(null);
        if(user==null || user.getSubscription()==null){
            return 0;
        }
        SubscriptionType userSubscriptionType = user.getSubscription().getSubscriptionType();
        int count=0;
        for (WebSeries webSeries : webSeriesList) {
            if (user.getAge() >= webSeries.getAgeLimit()) {
                // Elite can watch everything
                if (userSubscriptionType == SubscriptionType.ELITE) {
                    count++;
                }
                // Pro can watch Pro and Basic
                else if (userSubscriptionType == SubscriptionType.PRO &&
                        (webSeries.getSubscriptionType() == SubscriptionType.PRO ||
                                webSeries.getSubscriptionType() == SubscriptionType.BASIC)) {
                    count++;
                }
                // Basic can only watch Basic
                else if (userSubscriptionType == SubscriptionType.BASIC &&
                        webSeries.getSubscriptionType() == SubscriptionType.BASIC) {
                    count++;
                }
            }
        }

        return count;
    }


}
