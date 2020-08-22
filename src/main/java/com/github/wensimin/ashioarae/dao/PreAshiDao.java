package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.entity.PreAshi;
import com.github.wensimin.ashioarae.entity.SysUser;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface PreAshiDao extends CrudRepository<PreAshi, Long> {

    PreAshi findBySysUser(SysUser sysUser);

}
