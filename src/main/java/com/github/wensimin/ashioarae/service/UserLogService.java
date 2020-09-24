package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.dao.SysUserDao;
import com.github.wensimin.ashioarae.dao.UserLogDao;
import com.github.wensimin.ashioarae.entity.UserLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class UserLogService {
    private final UserLogDao userLogDao;
    private final SysUserDao sysUserDao;

    @Autowired
    public UserLogService(UserLogDao userLogDao, SysUserDao sysUserDao) {
        this.userLogDao = userLogDao;
        this.sysUserDao = sysUserDao;
    }

    public UserLog addLog(UserLog log, String username) {
        log.setUser(sysUserDao.findByUsername(username));
        return userLogDao.save(log);
    }

    @Transactional
    public List<UserLog> findLog(String username) {
        return userLogDao.findByUser(sysUserDao.findByUsername(username));
    }
}
