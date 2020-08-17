package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.dao.AshiTargetDao;
import com.github.wensimin.ashioarae.dao.SysUserDao;
import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.exception.AshiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

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
        AshioaraeInterface first = ashioaraeInterfaceList.stream().filter(s -> s.getType() == type).findFirst().orElse(null);
        if (first == null) {
            throw new AshiException("未找到服务");
        }
        first.updateHeadImage(ashi.getCookie(), file);
    }

}
