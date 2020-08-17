package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface AshiTargetDao extends CrudRepository<AshiTarget, Long> {

    List<AshiTarget> findBySysUser(SysUser sysUser);

    AshiTarget findBySysUserAndType(SysUser sysUser, @NotNull AshiType type);

}
