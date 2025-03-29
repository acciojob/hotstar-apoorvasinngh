package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto) {
        User user = userRepository.findById(subscriptionEntryDto.getUserId()).orElse(null);
        if (user == null) {
            return -1;
        }

        Subscription subscription = user.getSubscription();
        if (subscription == null) {
            subscription = new Subscription(); // Create new if doesn't exist
            subscription.setUser(user);
        }
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setStartSubscriptionDate(new Date()); // Fix: Set start date

        int totalAmount = 0;
        switch (subscriptionEntryDto.getSubscriptionType()) {
            case BASIC:
                totalAmount = 500 + 200 * subscriptionEntryDto.getNoOfScreensRequired();
                break;
            case PRO:
                totalAmount = 800 + 250 * subscriptionEntryDto.getNoOfScreensRequired();
                break;
            case ELITE:
                totalAmount = 1000 + 350 * subscriptionEntryDto.getNoOfScreensRequired();
                break;
        }

        subscription.setTotalAmountPaid(totalAmount);

        // Fix: Ensure the user object also contains the subscription
        user.setSubscription(subscription);

        // Save user & subscription together
        subscriptionRepository.save(subscription);
        userRepository.save(user);

        return totalAmount;
    }


    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")
        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository

        User user= userRepository.findById(userId).orElse(null);
        if(user==null || user.getSubscription()==null){
            throw new IllegalArgumentException("User or subscription not found");
        }
        Subscription subscription= user.getSubscription();
        SubscriptionType currentSubscriptionType= subscription.getSubscriptionType();
        int currentScreens=subscription.getNoOfScreensSubscribed();

        if(currentSubscriptionType==SubscriptionType.ELITE){
            throw new IllegalArgumentException("You cannot upgrade an elite subscription");
        }

        int extraAmount=0;
        if(currentSubscriptionType==SubscriptionType.BASIC){
            subscription.setSubscriptionType(SubscriptionType.PRO);
            extraAmount=(800+(250*currentScreens))-subscription.getTotalAmountPaid();
        } else if(currentSubscriptionType==SubscriptionType.PRO){
            subscription.setSubscriptionType(SubscriptionType.ELITE);
            extraAmount=(1000+(350*currentScreens))-subscription.getTotalAmountPaid();
        }
        subscription.setTotalAmountPaid(subscription.getTotalAmountPaid()+extraAmount);
        subscriptionRepository.save(subscription);
        return extraAmount;
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb

        List<Subscription> subscriptionList=subscriptionRepository.findAll();
        int totalRevenue=0;
        for(Subscription subscription:subscriptionList){
            totalRevenue+=subscription.getTotalAmountPaid();
        }
        return totalRevenue;
    }

}
