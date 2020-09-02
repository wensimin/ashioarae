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
 * twitter service
 */
@Service
public class TwitterAshiService implements AshioaraeInterface {
    private final HttpBuilder httpBuilder;

    private static final String HOME_URL = "https://twitter.com/home";
    private static final String ATTR_REGEX = "(?<=\"%s\":\").+?(?=\")";
    private static final String UPLOAD_URL = "https://upload.twitter.com/i/media/upload.json";
    private static final String HEAD_URL = "https://api.twitter.com/1.1/account/update_profile_image.json";

    @Autowired
    public TwitterAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        long mediaId = this.uploadFile(cookies, file);
        HttpHeaders headers = new HttpHeaders();
        TarCookie ct0 = HttpUtils.getAttrInCookie(cookies, "ct0");
        if (ct0 == null) {
            throw new CookieExpireException();
        }
        String token = ct0.getValue();
        // oauth2 认证令牌疑似全部公用同一个,令人震惊
        headers.add("authorization", "Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA");
        headers.add("x-csrf-token", token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media_id", mediaId);
        httpBuilder.builder().method(HttpMethod.POST)
                .url(HEAD_URL).Headers(headers).body(body)
                .cookies(cookies).proxy().start(String.class);
    }

    /**
     * 上传文件3步骤
     *
     * @param cookies cookies
     * @param file   文件
     * @return 文件上传后的media id
     */
    private long uploadFile(List<TarCookie> cookies, File file) {
        long fileSize = file.length();
        var uploadInitUrl = UPLOAD_URL + "?command=INIT&total_bytes=" + fileSize;
        HttpHeaders headers = new HttpHeaders();
        headers.add("referer", "https://twitter.com/settings/profile");
        var initRes = httpBuilder.builder().method(HttpMethod.POST)
                .url(uploadInitUrl).Headers(headers)
                .cookies(cookies).proxy().start(TwitterResponse.class);
        this.checkRes(initRes);
        long mediaId = initRes.getMediaId();
        var appendUrl = UPLOAD_URL + "?command=APPEND&media_id=" + mediaId + "&segment_index=0";
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("media", new FileSystemResource(file));
        var appendRes = httpBuilder.builder().method(HttpMethod.POST)
                .url(appendUrl).Headers(headers).body(body)
                .cookies(cookies).proxy().start(TwitterResponse.class);
        this.checkRes(appendRes);
        var finalizeUrl = UPLOAD_URL + "?command=FINALIZE&media_id=" + mediaId;
        headers = new HttpHeaders();
        headers.add("referer", "https://twitter.com/settings/profile");
        var finalizeRes = httpBuilder.builder().method(HttpMethod.POST)
                .url(finalizeUrl).Headers(headers)
                .cookies(cookies).proxy().start(TwitterResponse.class);
        this.checkRes(finalizeRes);
        mediaId = finalizeRes.getMediaId();
        return mediaId;
    }

    private void checkRes(TwitterResponse initRes) {
        if (initRes == null) {
            return;
        }
        if (!StringUtils.isEmpty(initRes.getError())) {
            throw new AshiException(this.getType() + ": " + initRes.getError());
        }
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        String html = httpBuilder.builder().url(HOME_URL).cookies(cookies).proxy().start(String.class);
        // 正则可能有命名空间问题,目前未发现问题
        String name = this.getAttr(html, "name");
        String headImage = this.getAttr(html, "profile_image_url_https");
        if (name == null) {
            throw new CookieExpireException(AshiType.twitter + " cookie可能失效,检查cookie");
        }
        headImage = headImage.replace("normal", "400x400");
        return new AshiData(name, headImage);
    }

    private String getAttr(String html, String attr) {
        return HttpUtils.RexHtml(html, String.format(ATTR_REGEX, attr));
    }

    @Override
    public AshiType getType() {
        return AshiType.twitter;
    }

    private static class TwitterResponse {
        @JsonAlias("media_id")
        private Long mediaId;
        @JsonAlias("media_id_string")
        private String mediaIdString;
        private String error;

        public Long getMediaId() {
            return mediaId;
        }

        public void setMediaId(Long mediaId) {
            this.mediaId = mediaId;
        }

        public String getMediaIdString() {
            return mediaIdString;
        }

        public void setMediaIdString(String mediaIdString) {
            this.mediaIdString = mediaIdString;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
