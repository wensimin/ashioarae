package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
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

import java.io.File;

/**
 * github ashi service
 */
@Service
public class GithubAshiService implements AshioaraeInterface {
    private static final String INFO_URL = "https://github.com/settings/profile";
    private static final String UPLOAD_URL_1 = "https://github.com/upload/policies/avatars";
    private static final String UPLOAD_URL_2 = "https://uploads.github.com/avatars";
    private static final String UPLOAD_URL_3 = "https://github.com/settings/avatars/%s";

    private static final String NICK_REX = "(?<=<input class=\"form-control\" type=\"text\" value=\").+?(?=\" name=\"user\\[profile_name]\" id=\"user_profile_name\" \\/>)";
    private static final String USER_ID_REX = "(?<=<meta name=\"octolytics-actor-id\" content=\").+?(?=\" \\/>)";
    private static final String SETTING_TOKEN_REX = "(?<=data-upload-policy-url=\"\\/upload\\/policies\\/avatars\"><input type=\"hidden\" value=\").+?(?=\" data-csrf=\"true\" class=\"js-data-upload-policy-url-csrf)";
    private static final String HEAD_REX = "(?<=<img class=\"avatar rounded-2 avatar-user\" src=\").+(?=\")";
    private static final String UPLOAD_TOKEN_REX = "(?<=name=\"authenticity_token\" value=\").+?(?=\")";

    @Override
    public void updateHeadImage(String cookie, File file) {
        var html = HttpUtils.get(INFO_URL, cookie, String.class, true);
        String userId = HttpUtils.RexHtml(html, USER_ID_REX);
        String settingToken = HttpUtils.RexHtml(html, SETTING_TOKEN_REX);
        UpLoadRes1 res1 = this.upload1(settingToken, userId, file, cookie);
        UploadRes2 res2 = this.upload2(res1, cookie, userId, file);
        var headHtml = HttpUtils.get(String.format(UPLOAD_URL_3, res2.getId()), cookie, String.class, true);
        String uploadToken = HttpUtils.RexHtml(headHtml, UPLOAD_TOKEN_REX);
        this.upload(res2, cookie, uploadToken);
    }

    /**
     * 最后确认头像
     *
     * @param res2        第二次请求res
     * @param cookie      cookie
     * @param uploadToken csrf token
     */
    private void upload(UploadRes2 res2, String cookie, String uploadToken) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        body.add("op", "save");
        body.add("authenticity_token", uploadToken);
        body.add("cropped_x", "0");
        body.add("cropped_y", "0");
        body.add("cropped_width", res2.getWidth());
        body.add("cropped_height", res2.getHeight());
        HttpUtils.post(String.format(UPLOAD_URL_3, res2.getId()), headers, body, cookie, String.class, true);
    }

    /**
     * 上传第二部分
     *
     * @param res1   第一部分的返回值
     * @param cookie cookie
     * @param userId 用户id
     * @param file   文件
     * @return 第二部分结果
     */
    private UploadRes2 upload2(UpLoadRes1 res1, String cookie, String userId, File file) {
        String token2 = res1.getNextToken();
        String auth = res1.getHeader().getAuth();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        headers.add("github-remote-auth", auth);
        body.add("authenticity_token", token2);
        body.add("owner_type", "User");
        body.add("size", file.length());
        body.add("content_type", "image/jpeg");
        body.add("file", new FileSystemResource(file));
        body.add("owner_id", userId);
        return HttpUtils.post(UPLOAD_URL_2, headers, body, cookie, UploadRes2.class, true);
    }

    /**
     * 上传第一步
     *
     * @param settingToken 设置页 csrf token
     * @param userId       用户id
     * @param file         文件
     * @param cookie       cookie
     * @return 第一步返回值
     */
    private UpLoadRes1 upload1(String settingToken, String userId, File file, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("authenticity_token", settingToken);
        body.add("owner_type", "User");
        body.add("owner_id", userId);
        body.add("size", file.length());
        body.add("content_type", "image/jpeg");
        body.add("name", file.getName());
        return HttpUtils.post(UPLOAD_URL_1, headers, body, cookie, UpLoadRes1.class, true);
    }

    @Override
    public void updateNickname(String cookie, String nickname) {
        throw new AshiException("github 昵称修改暂不支持");
    }

    @Override
    public AshiData getInfo(String cookie) {
        var html = HttpUtils.get(INFO_URL, cookie, String.class, true);
        var nick = HttpUtils.RexHtml(html, NICK_REX);
        if (nick == null) {
            throw new CookieExpireException("github cookie可能失效");
        }
        var headImage = HttpUtils.RexHtml(html, HEAD_REX);
        return new AshiData(nick, headImage);
    }

    @Override
    public AshiType getType() {
        return AshiType.github;
    }

    private static class UpLoadRes1 {
        @JsonAlias("upload_authenticity_token")
        private String nextToken;
        private ResHeader header;

        public String getNextToken() {
            return nextToken;
        }

        public void setNextToken(String nextToken) {
            this.nextToken = nextToken;
        }

        public ResHeader getHeader() {
            return header;
        }

        public void setHeader(ResHeader header) {
            this.header = header;
        }

        private static class ResHeader {
            @JsonAlias("GitHub-Remote-Auth")
            private String auth;

            public String getAuth() {
                return auth;
            }

            public void setAuth(String auth) {
                this.auth = auth;
            }
        }
    }

    private static class UploadRes2 {
        private String id;
        private int width;
        private int height;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

}
