package com.github.wensimin.ashioarae.service;

import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.exception.AshiException;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class BilibiliAshiService implements AshioaraeInterface {

    private static final String HEAD_API = "https://api.bilibili.com/x/member/web/face/update";

    @Override
    public void updateHeadImage(String cookie, File file) {
        var cookieMap = HttpUtils.cookie2map(cookie);
        var csrf = cookieMap.get("bili_jct");
        var url = HEAD_API + "?csrf=" + csrf;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("cookie", cookie);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        try {
            body.add("face", Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("file error");
        }
        body.add("dopost", "save");
        body.add("Displayrank", "1000");
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<BilibiliResponse> response = restTemplate
                .postForEntity(url, requestEntity, BilibiliResponse.class);
        var res = response.getBody();
        if (res == null) {
            throw new AshiException("res null");
        }
        if (res.getCode() != BilibiliResponse.SUCCESS_CODE) {
            throw new AshiException(res.getMessage());
        }
    }

    @Override
    public void updateNickname(String cookie, String nickname) {
        //TODO
    }

    @Override
    public boolean checkCookieExpire(String cookie) {
        //TODO
        return false;
    }

    @Override
    public AshiType getType() {
        return AshiType.bilibili;
    }

    /**
     * b站返回对象
     */
    public static class BilibiliResponse {
        private int code;
        public static final int SUCCESS_CODE = 0;
        private String message;
        private String ttl;
        private String data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTtl() {
            return ttl;
        }

        public void setTtl(String ttl) {
            this.ttl = ttl;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
