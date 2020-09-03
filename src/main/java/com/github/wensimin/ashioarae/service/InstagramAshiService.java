package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpBuilder;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * instagram service
 * 接口非常简单直观 好评
 */
@Service
public class InstagramAshiService implements AshioaraeInterface {
    private final HttpBuilder httpBuilder;

    private static final String INFO_URL = "https://www.instagram.com/accounts/edit/";
    private static final String UPLOAD_URL = "https://www.instagram.com/accounts/web_change_profile_picture/";

    private static final String PROP_REGEX = "(?<=\"%s\":\").+?(?=\")";

    @Autowired
    public InstagramAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        var html = this.infoRequest(cookies);
        var nickName = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "full_name"));
        if (StringUtils.isEmpty(nickName)) {
            throw new CookieExpireException();
        }
        var headImage = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "profile_pic_url_hd"));
        headImage = headImage.replace("\\u0026", "&");
        return new AshiData(nickName, headImage);
    }

    private String infoRequest(List<TarCookie> cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority", "www.instagram.com");
        headers.add("cache-control", "max-age=0");
        headers.add("upgrade-insecure-requests", "1");
        headers.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.add("sec-fetch-site", "same-origin");
        headers.add("sec-fetch-mode", "navigate");
        headers.add("sec-fetch-user", "?1");
        headers.add("sec-fetch-dest", "document");
        headers.add("accept-language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6");
        return httpBuilder.builder().url(INFO_URL).Headers(headers)
                .cookies(cookies).proxy().start(String.class);
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        var html = this.infoRequest(cookies);
        var csrfToken = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "csrf_token"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("x-csrftoken", csrfToken);
        headers.add("x-ig-app-id", "936619743392459");
        headers.add("origin", "https://www.instagram.com");
        headers.add("sec-fetch-site", "same-origin");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-dest", "empty");
        headers.add("referer", "https://www.instagram.com/accounts/edit/");
        headers.add("accept-language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("profile_pic", new FileSystemResource(file));
        var res = httpBuilder.builder().method(HttpMethod.POST).url(UPLOAD_URL)
                .Headers(headers).body(body).cookies(cookies)
                .proxy().start(InstagramResponse.class);
        this.checkRes(res);
    }

    private void checkRes(InstagramResponse res) {
        if (!res.getStatus().equals("ok")) {
            throw new AshiException("instagram error");
        }
    }

    @Override
    public AshiType getType() {
        return AshiType.instagram;
    }

    private static class InstagramResponse {
        private String status;
        private String id;
        @JsonAlias("has_profile_pic")
        private Boolean hasProfilePic;
        @JsonAlias("changed_profile")
        private Boolean changedProfile;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Boolean getHasProfilePic() {
            return hasProfilePic;
        }

        public void setHasProfilePic(Boolean hasProfilePic) {
            this.hasProfilePic = hasProfilePic;
        }

        public Boolean getChangedProfile() {
            return changedProfile;
        }

        public void setChangedProfile(Boolean changedProfile) {
            this.changedProfile = changedProfile;
        }
    }
}
