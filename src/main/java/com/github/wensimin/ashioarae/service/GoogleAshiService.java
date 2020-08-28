package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;

@Service
public class GoogleAshiService implements AshioaraeInterface {
    private static final String INFO_URL = "https://myaccount.google.com/";

    private static final String NICK_REGEX = "(?<=<div class=\"gb_ub gb_vb\">).+?(?=<\\/div>)";
    private static final String HEAD_REGEX = "(?<=<img class=\"gb_7b gb_nb\" .+ data-src=\").+?(?=\" alt=\"\" aria-hidden=\"true\">)";

    @Override
    public void updateHeadImage(String cookie, File file) {
        throw new AshiException("todo");
    }

    @Override
    public void updateNickname(String cookie, String nickname) {
        throw new AshiException("todo");
    }

    @Override
    public AshiData getInfo(String cookie) {
        String html = HttpUtils.get(INFO_URL, cookie, String.class, true);
        String nickname = HttpUtils.RexHtml(html, NICK_REGEX);
        if (StringUtils.isEmpty(nickname)) {
            throw new CookieExpireException();
        }
        String headImage = HttpUtils.RexHtml(html, HEAD_REGEX);
        headImage = headImage.replace("s48", "s400");
        return new AshiData(nickname, headImage);
    }

    @Override
    public AshiType getType() {
        return AshiType.google;
    }
}
