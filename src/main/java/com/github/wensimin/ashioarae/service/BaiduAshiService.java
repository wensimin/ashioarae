package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BaiduAshiService implements AshioaraeInterface {
    private final HttpBuilder httpBuilder;

    private static final String INFO_URL = "http://tieba.baidu.com/f/user/json_userinfo";

    @Autowired
    public BaiduAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        var res = httpBuilder.builder().url(INFO_URL).cookies(cookies)
                .converter(new BaiduHttpMessageConverter())
                .start(BaiduInfoResponse.class);
        var data = res.getData();
        if (data == null) {
            throw new CookieExpireException();
        }
        return new AshiData(data.getNick(), "https://himg.bdimg.com/sys/portrait/item/" + data.getHeadImg());
    }

    @Override
    public AshiType getType() {
        return AshiType.baidu;
    }

    private static final class BaiduInfoResponse {
        private String no;
        private Data data;

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        private static class Data {
            @JsonAlias("session_id")
            private String sessionId;
            @JsonAlias("user_name_weak")
            private String nick;
            @JsonAlias("user_portrait")
            private String headImg;

            public String getSessionId() {
                return sessionId;
            }

            public void setSessionId(String sessionId) {
                this.sessionId = sessionId;
            }

            public String getNick() {
                return nick;
            }

            public void setNick(String nick) {
                this.nick = nick;
            }

            public String getHeadImg() {
                return headImg;
            }

            public void setHeadImg(String headImg) {
                this.headImg = headImg;
            }
        }
    }

    private static class BaiduHttpMessageConverter extends MappingJackson2HttpMessageConverter {
        @Override
        public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
            supportedMediaTypes = new ArrayList<>();
            supportedMediaTypes.add(MediaType.TEXT_HTML);
            super.setSupportedMediaTypes(supportedMediaTypes);
        }
    }
}
