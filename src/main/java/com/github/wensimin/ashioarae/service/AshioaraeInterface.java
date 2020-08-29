package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;

import java.io.File;
import java.util.List;

/**
 * 同步service的接口类
 */
public interface AshioaraeInterface {

    /**
     * 更新头像
     *
     * @param cookies cookies
     * @param file   头像文件
     */
    default void updateHeadImage(List<TarCookie> cookies, File file) {
        throw new AshiException("todo");
    }

    /**
     * 更新昵称
     *
     * @param cookies   cookies
     * @param nickname 昵称
     */
    default void updateNickname(List<TarCookie> cookies, String nickname) {
        throw new AshiException("昵称 未实现");
    }

    /**
     * 获取用户信息
     *
     * @param cookies cookie
     * @return 用户信息
     */
    default AshiData getInfo(List<TarCookie> cookies) {
        throw new AshiException("todo");
    }

    /**
     * 获取目标站类型
     *
     * @return 目标站类型
     */
    AshiType getType();
}
