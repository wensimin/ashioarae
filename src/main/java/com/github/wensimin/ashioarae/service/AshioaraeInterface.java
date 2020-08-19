package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.service.enums.AshiType;

import java.io.File;

/**
 * 同步service的接口类
 */
public interface AshioaraeInterface {

    /**
     * 更新头像
     *
     * @param cookie cookie
     * @param file   头像文件
     */
    void updateHeadImage(String cookie, File file);

    /**
     * 更新昵称
     *
     * @param cookie   cookie
     * @param nickname 昵称
     */
    void updateNickname(String cookie, String nickname);

    /**
     * 检查cookie的有效性
     *
     * @param cookie 检查cookie的有效性
     * @return 是否有效
     */
    AshiData getInfo(String cookie);

    /**
     * 获取目标站类型
     *
     * @return 目标站类型
     */
    AshiType getType();
}
