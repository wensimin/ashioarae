package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.TarCookie;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import com.github.wensimin.ashioarae.service.utils.HttpBuilder;
import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaiduAshiService implements AshioaraeInterface {
    private static final Logger logger = LoggerFactory.getLogger(WeiboAshiService.class);
    private final HttpBuilder httpBuilder;

    private static final String INFO_URL = "http://tieba.baidu.com/f/user/json_userinfo";
    private static final String UPDATE_PAGE_URL = "https://passport.baidu.com/v3/ucenter/accountportrait";
    private static final String UPDATE_URL = "https://passport.baidu.com/sys/preview";
    private static final String SELECT_URL = "https://passport.baidu.com/sys/corpupload";

    private static final String PROP_REGEX = "(?<=var %s = ').+?(?=';)";
    // 上传确认成功的正则
    private static final String UPLOAD_RIGHT_REGEX = "errno=0";
    // pic id 正则
    private static final String PIC_REGEX = "(?<=picId=).+?(?=\"|\\)|&)";

    @Autowired
    public BaiduAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        cookies = cookies.stream()
                .filter((cookie) -> cookie.getName().equals("BDUSS")
                        || cookie.getName().equals("BDUSS_BFESS")
                        || (cookie.getName().equals("STOKEN") && cookie.getDomain().equals(".tieba.baidu.com")))
                .collect(Collectors.toList());
        var res = httpBuilder.builder().url(INFO_URL).cookies(cookies)
                .converter(new BaiduHttpMessageConverter())
                .start(BaiduInfoResponse.class);
        if (res == null) {
            throw new CookieExpireException();
        }
        var data = res.getData();
        return new AshiData(data.getNick(), "https://himg.bdimg.com/sys/portrait/item/" + data.getHeadImg());
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        var html = httpBuilder.builder().url(UPDATE_PAGE_URL).cookies(cookies)
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-User", "?1")
                .header("Sec-Fetch-Dest", "document")
                .header("Referer", "https://passport.baidu.com/v6/ucenter")
                .header("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6")
                .start(String.class);
        var token = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "bdstoken"));
        var sign = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "psign"));
        var picId = this.upload(file, cookies, token, sign);
        this.select(picId, file, token, sign, cookies);
    }

    /**
     * 进行选择
     */
    private void select(String picId, File file, String token, String sign, List<TarCookie> cookies) {
        BufferedImage imageFile;
        try {
            imageFile = ImageIO.read(file);
        } catch (IOException e) {
            throw new AshiException("file error");
        }
        var url = SELECT_URL +
                // ajax callback 写死应该没有问题
                "?callback=jQuery190007022220999028206_1599049251071" +
                "&coordX=0" +
                "&coordY=0" +
                "&coordW=" + imageFile.getWidth() +
                "&coordH=" + imageFile.getHeight() +
                "&psign=" + sign +
                "&bdstoken=" + token +
                "&picId=" + picId;
        var res = httpBuilder.builder().url(url)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Dest", "empty")
                .header("Referer", "https://passport.baidu.com/v3/ucenter/accountportrait")
                .header("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6")
                .header("Connection", "keep-alive")
                .header("Accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01")
                .cookies(cookies).start(String.class);
        logger.info(res);
    }

    /**
     * 上传
     */
    private String upload(File file, List<TarCookie> cookies, String token, String sign) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("bdstoken", token);
        body.add("psign", sign);
        body.add("staticpage", "https://passport.baidu.com/passApi/html/jump.html");
        body.add("file", new FileSystemResource(file));
        var html = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(UPDATE_URL + "/" + sign)
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-User", "?1")
                .header("Sec-Fetch-Dest", "iframe")
                .header("Referer", "https://passport.baidu.com/v3/ucenter/accountportrait")
                .header("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6")
                .header("Cache-Control", "max-age=0")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Origin", "https://passport.baidu.com")
                .header("Connection", "keep-alive")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .cookies(cookies)
                .start(String.class);
        return this.checkUpload(html);
    }

    /**
     * 检查upload的结果
     *
     * @param html html
     */
    private String checkUpload(String html) {
        var res = HttpUtils.RexHtml(html, UPLOAD_RIGHT_REGEX);
        if (StringUtils.isEmpty(res)) {
            throw new AshiException("error:" + html);
        }
        return HttpUtils.RexHtml(html, PIC_REGEX);
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
