package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GoogleAshiService implements AshioaraeInterface {
    private static final String INFO_URL = "https://myaccount.google.com/";

    private static final String NICK_REGEX = "(?<=<div class=\"gb_ub gb_vb\">).+?(?=<\\/div>)";
    private static final String HEAD_REGEX = "(?<=<img class=\"gb_7b gb_nb\" .+ data-src=\").+?(?=\" alt=\"\" aria-hidden=\"true\">)";


    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        String html = HttpUtils.get(INFO_URL, cookies, String.class, true);
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
