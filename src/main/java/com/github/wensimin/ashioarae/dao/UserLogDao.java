package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.entity.UserLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserLogDao extends CrudRepository<UserLog, Long> {
    List<UserLog> findByUser(SysUser user);
}
