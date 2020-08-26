package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("user")
public class UserController {
    private final SysUserService userService;

    @Autowired
    public UserController(SysUserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("register")
    public SysUser register(@Valid @RequestBody SysUser user) {
        return userService.register(user);
    }

    @GetMapping
    public String username(Principal principal) {
        return principal.getName();
    }
}
