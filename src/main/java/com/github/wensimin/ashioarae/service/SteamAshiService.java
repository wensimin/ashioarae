package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        var cookieMap = HttpUtils.cookie2map(cookie);
        var sessionId = cookieMap.get("sessionid");
        var sId = cookieMap.get("steamRememberLogin");
        sId = sId.substring(0, sId.indexOf("%7C%7C"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("avatar", new FileSystemResource(file));
        body.add("type", "player_avatar_image");
        body.add("sId", sId);
        body.add("sessionid", sessionId);
        body.add("doSub", 1);
        body.add("json", 1);
        UpdateResponse res = HttpUtils.post(HEAD_URL, headers, body, cookie, UpdateResponse.class, true);
        this.checkRes(res);
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

    private void checkRes(UpdateResponse res) {
        if (!res.getSuccess()) {
            throw new AshiException("steam 更新头像失败");
        }
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

    private static class UpdateResponse {
        private Boolean success;
        private JsonNode Images;
        private String hash;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public JsonNode getImages() {
            return Images;
        }

        public void setImages(JsonNode images) {
            Images = images;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }
    }
}
