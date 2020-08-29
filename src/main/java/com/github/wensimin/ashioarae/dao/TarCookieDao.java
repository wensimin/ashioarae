package com.github.wensimin.ashioarae.dao;

import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.entity.TarCookie;
import org.springframework.data.repository.CrudRepository;

public interface TarCookieDao extends CrudRepository<TarCookie, Long> {
    void deleteByAshiTarget(AshiTarget ashiTarget);
}
