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
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * twitter service
 */
@Service
public class TwitterAshiService implements AshioaraeInterface {

    private static final String HOME_URL = "https://twitter.com/home";
    private static final String ATTR_REGEX = "(?<=\"%s\":\").+?(?=\")";
    private static final String UPLOAD_URL = "https://upload.twitter.com/i/media/upload.json";
    private static final String HEAD_URL = "https://api.twitter.com/1.1/account/update_profile_image.json";

    @Override
    public void updateHeadImage(String cookie, File file) {
        long mediaId = this.uploadFile(cookie, file);
        HttpHeaders headers = new HttpHeaders();
        String token = HttpUtils.cookie2map(cookie).get("ct0");
        // oauth2 认证令牌疑似全部公用同一个,令人震惊
        headers.add("authorization", "Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA");
        headers.add("x-csrf-token", token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media_id", mediaId);
        HttpUtils.post(HEAD_URL, headers, body, cookie, String.class, true);
    }

    /**
     * 上传文件3步骤
     *
     * @param cookie cookie
     * @param file   文件
     * @return 文件上传后的media id
     */
    private long uploadFile(String cookie, File file) {
        long fileSize = file.length();
        var uploadInitUrl = UPLOAD_URL + "?command=INIT&total_bytes=" + fileSize;
        HttpHeaders headers = new HttpHeaders();
        headers.add("referer", "https://twitter.com/settings/profile");
        var initRes = HttpUtils.post(uploadInitUrl, headers, cookie, TwitterResponse.class, true);
        this.checkRes(initRes);
        long mediaId = initRes.getMediaId();
        var appendUrl = UPLOAD_URL + "?command=APPEND&media_id=" + mediaId + "&segment_index=0";
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("media", new FileSystemResource(file));
        var appendRes = HttpUtils.post(appendUrl, headers, body, cookie, TwitterResponse.class, true);
        this.checkRes(appendRes);
        var finalizeUrl = UPLOAD_URL + "?command=FINALIZE&media_id=" + mediaId;
        headers = new HttpHeaders();
        headers.add("referer", "https://twitter.com/settings/profile");
        var finalizeRes = HttpUtils.post(finalizeUrl, headers, cookie, TwitterResponse.class, true);
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
    public void updateNickname(String cookie, String nickname) {
        throw new AshiException("twitter 昵称TODO");
    }

    @Override
    public AshiData getInfo(String cookie) {
        String html = HttpUtils.get(HOME_URL, cookie, String.class, true);
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
        return Optional.of(Pattern.compile(String.format(ATTR_REGEX, attr))).
                map(p -> p.matcher(html))
                .filter(Matcher::find)
                .map(Matcher::group)
                .orElse(null);
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
