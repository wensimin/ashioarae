package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.entity.UserLog;
import org.springframework.data.repository.CrudRepository;

public interface UserLogDao extends CrudRepository<UserLog, Long> {
    UserLog findByUser(SysUser user);
}
