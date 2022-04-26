package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpBuilder;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * b站ashi service
 */
@Service
public class BilibiliAshiService implements AshioaraeInterface {
    private final HttpBuilder httpBuilder;
    private static final String UPDATE_HEAD_API = "https://api.bilibili.com/x/member/web/face/update";
    private static final String INFO_API = "https://api.bilibili.com/x/member/web/account";
    private static final String HEAD_API = "https://account.bilibili.com/pendant/current";

    @Autowired
    public BilibiliAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }


    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        var csrf = HttpUtils.getAttrInCookie(cookies, "bili_jct");
        var url = UPDATE_HEAD_API + "?csrf=" + csrf.getValue();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("face", Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("file error");
        }
        body.add("dopost", "save");
        body.add("Displayrank", "1000");
        var res = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(url)
                .headers(headers)
                .body(body)
                .cookies(cookies)
                .run(BilibiliResponse.class);
        this.checkRes(res);
    }



    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        BilibiliResponse infoResponse = httpBuilder.builder().url(INFO_API).cookies(cookies).run(BilibiliResponse.class);
        BilibiliResponse headResponse = httpBuilder.builder().url(HEAD_API).cookies(cookies).run(BilibiliResponse.class);
        checkRes(infoResponse);
        checkRes(headResponse);
        String name = Optional.of(infoResponse)
                .map(BilibiliResponse::getData)
                .map(d -> d.get("uname"))
                .map(JsonNode::asText)
                .orElse("");
        String headImage = Optional.of(headResponse)
                .map(BilibiliResponse::getData)
                .map(d -> d.get("face_url"))
                .map(JsonNode::asText)
                .orElse("");
        return new AshiData(name, headImage);
    }

    @Override
    public AshiType getType() {
        return AshiType.bilibili;
    }

    /**
     * 检查结果
     *
     * @param res response
     */
    private void checkRes(BilibiliResponse res) {
        if (res == null) {
            throw new AshiException("bilibili res null");
        }
        if (res.getCode() != BilibiliResponse.SUCCESS_CODE) {
            if (res.getCode() == BilibiliResponse.EXPIRE_CODE) {
                throw new CookieExpireException(AshiType.bilibili + " cookie过期");
            }
            throw new AshiException(res.getMessage());
        }
    }

    /**
     * b站返回对象
     */
    public static class BilibiliResponse {
        private int code;
        public static final int SUCCESS_CODE = 0;
        public static final int EXPIRE_CODE = -101;
        private String message;
        private String ttl;
        private JsonNode data;

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

        public JsonNode getData() {
            return data;
        }

        public void setData(JsonNode data) {
            this.data = data;
        }
    }
}
