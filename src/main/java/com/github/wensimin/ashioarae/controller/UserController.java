package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final SysUserService userService;

    @Autowired
    public UserController(SysUserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public SysUser register(@RequestBody SysUser user) {
        return userService.register(user);
    }
}
