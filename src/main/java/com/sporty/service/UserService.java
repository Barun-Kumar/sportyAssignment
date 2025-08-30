package com.sporty.service;

import com.sporty.model.BetDTO;
import com.sporty.model.UserDTO;
import com.sporty.repository.InMemoryUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private InMemoryUserRepository repository;

    public BigDecimal getUserBalance(String userId){
       Optional<UserDTO> userOptional =repository.get(userId);
       if(userOptional.isPresent()){
           UserDTO user =userOptional.get();
           return user.getBalance();
       }
       return BigDecimal.ZERO;
    }

    public Optional<UserDTO> getUser(String userId){
       return repository.get(userId);
    }


    public void updateUserBalance(BetDTO bet) {
        String userId = bet.getUserId();
        Optional<UserDTO> user = getUser(userId);

        if(user.isPresent()){
            UserDTO userDTO = user.get();
            userDTO.setBalance(userDTO.getBalance().subtract(bet.getStake()));
            repository.update(user.get());
        }
    }
}
