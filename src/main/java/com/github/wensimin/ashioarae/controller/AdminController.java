package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.entity.UserLog;
import com.github.wensimin.ashioarae.service.SysUserService;
import com.github.wensimin.ashioarae.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("admin")
public class AdminController {
    private final SysUserService userService;
    private final UserLogService userLogService;

    @Autowired
    public AdminController(SysUserService userService, UserLogService userLogService) {
        this.userService = userService;
        this.userLogService = userLogService;
    }

    @PostMapping("register")
    public SysUser register(@Valid @RequestBody SysUser user) {
        return userService.register(user);
    }

    @GetMapping("log/{username}")
    public List<UserLog> log(@PathVariable String username) {
        return userLogService.findLog(username);
    }
}
