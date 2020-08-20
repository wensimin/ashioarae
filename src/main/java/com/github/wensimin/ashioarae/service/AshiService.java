package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.dao.AshiTargetDao;
import com.github.wensimin.ashioarae.dao.SysUserDao;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class AshiService {
    @Value("${system.file.base-path}")
    private String fileBasePath;
    private final AshiTargetDao ashiTargetDao;
    private final SysUserDao sysUserDao;
    private final List<AshioaraeInterface> ashioaraeInterfaceList;

    @Autowired
    public AshiService(AshiTargetDao ashiTargetDao, SysUserDao sysUserDao, List<AshioaraeInterface> ashioaraeInterfaceList) {
        this.ashiTargetDao = ashiTargetDao;
        this.sysUserDao = sysUserDao;
        this.ashioaraeInterfaceList = ashioaraeInterfaceList;
    }

    /**
     * 保存ashi target
     *
     * @param ashiTarget ashiTarget
     * @param username   当前用户
     * @return ashi对象
     */
    public AshiTarget save(AshiTarget ashiTarget, String username) {
        var user = sysUserDao.findByUsername(username);
        var oldAshi = ashiTargetDao.findBySysUserAndType(user, ashiTarget.getType());
        if (oldAshi != null && !oldAshi.getId().equals(ashiTarget.getId())) {
            ashiTarget.setId(oldAshi.getId());
        }
        ashiTarget.setSysUser(user);
        return ashiTargetDao.save(ashiTarget);
    }

    /**
     * 进行同步头像
     *
     * @param type     同步类型
     * @param username 当前用户
     */
    public void ashiHead(AshiType type, String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = ashiTargetDao.findBySysUserAndType(user, type);
        File file = new File(fileBasePath + "/" + ashi.getHeadImage());
        AshioaraeInterface service = getService(type);
        service.updateHeadImage(ashi.getCookie(), file);
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
        return service.getInfo(ashi.getCookie());
    }

    /**
     * 获取ashi类型的数据
     *
     * @param username 当前用户
     * @return ashiData
     */
    public AshiData ashiInfo(String username) {
        var user = sysUserDao.findByUsername(username);
        var ashi = ashiTargetDao.findBySysUserAndType(user, AshiType.ashioarae);
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


}
