package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.entity.UserLog;
import com.github.wensimin.ashioarae.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserLogService userLogService;

    @Autowired
    public UserController(UserLogService userLogService) {
        this.userLogService = userLogService;
    }

    @PostMapping("log")
    public UserLog addLog(@Valid @RequestBody UserLog log, Principal principal) {
        return userLogService.addLog(log, principal.getName());
    }

    @GetMapping
    public String username(Principal principal) {
        return principal.getName();
    }
}
