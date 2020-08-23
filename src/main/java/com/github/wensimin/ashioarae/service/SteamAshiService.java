package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * steam service
 */
@Service
public class SteamAshiService implements AshioaraeInterface {
    //解析用户信息rex
    private static final Pattern INFO_REGEX = Pattern.compile("(?<=data-userinfo=\").+(?=\")");
    private static final String HOME_URL = "https://steamcommunity.com/";
    private static final String HEAD_URL = "https://steamcommunity.com/actions/FileUploader/";
    private static final String INFO_URL = "https://steamcommunity.com/miniprofile/%s/json";

    @Override
    public void updateHeadImage(String cookie, File file) {
        throw new AshiException("steam 同步头像暂不支持");
    }

    @Override
    public void updateNickname(String cookie, String nickname) {
        throw new AshiException("steam 同步昵称暂不支持");
    }

    @Override
    public AshiData getInfo(String cookie) {
        String html = HttpUtils.get(HOME_URL, cookie, String.class, true);
        Matcher matcher = INFO_REGEX.matcher(html);
        if (!matcher.find()) {
            throw new RuntimeException("steam 捕获出错,检查api");
        }
        String info = matcher.group();
        if ("[]".equals(info)) {
            throw new CookieExpireException("steam:未能获取当前用户信息,检查cookie");
        }
        info = HtmlUtils.htmlUnescape(info);
        Map<String, String> infoMap = HttpUtils.json2Map(info);
        String accountId = infoMap.get("accountid");
        String infoUrl = String.format(INFO_URL, accountId);
        SteamInfo steamInfo = HttpUtils.get(infoUrl, cookie, SteamInfo.class, true);
        return new AshiData(steamInfo.getNickname(), steamInfo.getHeadImage());
    }

    @Override
    public AshiType getType() {
        return AshiType.steam;
    }

    private static class SteamInfo {
        @JsonAlias("avatar_url")
        private String headImage;
        @JsonAlias("persona_name")
        private String nickname;

        public String getHeadImage() {
            return headImage;
        }

        public void setHeadImage(String headImage) {
            this.headImage = headImage;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
