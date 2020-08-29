package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.dao.AshiTargetDao;
import com.github.wensimin.ashioarae.dao.PreAshiDao;
import com.github.wensimin.ashioarae.dao.SysUserDao;
import com.github.wensimin.ashioarae.dao.TarCookieDao;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.entity.PreAshi;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.List;

@Service
public class AshiService {
    @Value("${system.file.base-path}")
    private String fileBasePath;
    private final AshiTargetDao ashiTargetDao;
    private final SysUserDao sysUserDao;
    private final PreAshiDao preAshiDao;
    private final TarCookieDao tarCookieDao;
    private final List<AshioaraeInterface> ashioaraeInterfaceList;

    @Autowired
    public AshiService(AshiTargetDao ashiTargetDao, SysUserDao sysUserDao, PreAshiDao preAshiDao, List<AshioaraeInterface> ashioaraeInterfaceList, TarCookieDao tarCookieDao) {
        this.ashiTargetDao = ashiTargetDao;
        this.sysUserDao = sysUserDao;
        this.preAshiDao = preAshiDao;
        this.ashioaraeInterfaceList = ashioaraeInterfaceList;
        this.tarCookieDao = tarCookieDao;
    }

    /**
     * 保存ashi target
     *
     * @param ashiTarget ashiTarget
     * @param username   当前用户
     * @return ashi对象
     */
    @Transactional
    public AshiTarget save(AshiTarget ashiTarget, String username) {
        var user = sysUserDao.findByUsername(username);
        var oldAshi = ashiTargetDao.findBySysUserAndType(user, ashiTarget.getType());
        var cookies = ashiTarget.getCookies();
        if (oldAshi != null && !oldAshi.getId().equals(ashiTarget.getId())) {
            ashiTarget.setId(oldAshi.getId());
        }
        ashiTarget.setSysUser(user);
        ashiTarget = ashiTargetDao.save(ashiTarget);
        tarCookieDao.deleteByAshiTarget(ashiTarget);
        for (var c : cookies) {
            c.setAshiTarget(ashiTarget);
            tarCookieDao.save(c);
        }
        return ashiTarget;
    }

    /**
     * 进行同步头像
     *
     * @param type     同步类型
     * @param username 当前用户
     */
    public void updateAshiHead(AshiType type, String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = ashiTargetDao.findBySysUserAndType(user, type);
        var preAshi =preAshiDao.findBySysUser(user);
        File file = new File(fileBasePath + "/" + preAshi.getHeadImage());
        AshioaraeInterface service = getService(type);
        service.updateHeadImage(ashi.getCookies(), file);
    }

    /**
     * 进行同步昵称
     *
     * @param type     同步类型
     * @param username 当前用户
     */
    public void updateAshiNick(AshiType type, String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = ashiTargetDao.findBySysUserAndType(user, type);
        var preAshi =preAshiDao.findBySysUser(user);
        AshioaraeInterface service = getService(type);
        service.updateNickname(ashi.getCookies(), preAshi.getNickname());
    }

    /**
     * 获取对应类型的ashiData
     *
     * @param type     类型
     * @param username 当前用户
     * @return ashiData
     */
    public AshiData ashiInfo(AshiType type, String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = ashiTargetDao.findBySysUserAndType(user, type);
        AshioaraeInterface service = getService(type);
        return service.getInfo(ashi.getCookies());
    }

    /**
     * 获取ashi类型的数据
     *
     * @param username 当前用户
     * @return ashiData
     */
    public AshiData ashiInfo(String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = preAshiDao.findBySysUser(user);
        if (ashi == null) {
            return null;
        }
        return new AshiData(ashi.getNickname(), ashi.getHeadImage());
    }

    /**
     * 获取类型对应服务
     *
     * @param type 类型
     * @return 服务
     */
    private AshioaraeInterface getService(AshiType type) {
        AshioaraeInterface first = ashioaraeInterfaceList.stream().filter(s -> s.getType() == type).findFirst().orElse(null);
        if (first == null) {
            throw new AshiException("未找到服务");
        }
        return first;
    }


    /**
     * 保存preAshi信息
     * @param preAshi 预更新ashi
     * @param name 当前用户
     * @return preAshi对象
     */
    public PreAshi savePreAshi(PreAshi preAshi, String name) {
        var user = sysUserDao.findByUsername(name);
        var oldAshi = preAshiDao.findBySysUser(user);
        if (oldAshi != null && !oldAshi.getId().equals(preAshi.getId())) {
            preAshi.setId(oldAshi.getId());
        }
        preAshi.setSysUser(user);
        return preAshiDao.save(preAshi);
    }
}
