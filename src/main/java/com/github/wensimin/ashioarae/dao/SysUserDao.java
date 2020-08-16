package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.SysUser;
import org.springframework.data.repository.CrudRepository;

public interface SysUserDao extends CrudRepository<SysUser, Long> {
    SysUser findByUsername(String username);
}
