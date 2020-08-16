package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.dao.SysUserDao;
import com.github.wensimin.ashioarae.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserService implements UserDetailsService {
    private final SysUserDao sysUserDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SysUserService(SysUserDao sysUserDao, PasswordEncoder passwordEncoder) {
        this.sysUserDao = sysUserDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserDao.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return user;
    }

    public SysUser register(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = this.sysUserDao.save(user);
        user.setPassword(null);
        return user;
    }
}
