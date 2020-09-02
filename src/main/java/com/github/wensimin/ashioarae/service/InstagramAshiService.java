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
        var html = httpBuilder.builder().url(INFO_URL).cookies(cookies).proxy().start(String.class);
        var nickName = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "full_name"));
        if (StringUtils.isEmpty(nickName)) {
            throw new CookieExpireException();
        }
        var headImage = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "profile_pic_url_hd"));
        headImage = headImage.replace("\\u0026", "&");
        return new AshiData(nickName, headImage);
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        var html = httpBuilder.builder().url(INFO_URL).cookies(cookies).proxy().start(String.class);
        var csrfToken = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "csrf_token"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("x-csrftoken", csrfToken);
        headers.add("x-ig-app-id", "936619743392459");
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
