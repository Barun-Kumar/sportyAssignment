package com.sporty.controller;


import com.sporty.model.UserDTO;
import com.sporty.repository.UserRepo;
import com.sporty.service.UserService;
import com.sporty.util.MoneyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;


@RestController
@RequestMapping("/v1/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/balance")
    public BigDecimal balance(@PathVariable("userId") String userId) {
       return userService.getUserBalance(userId);
    }
}