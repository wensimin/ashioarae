package com.github.wensimin.ashioarae.service;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

@Service
public class WeiboAshiService implements AshioaraeInterface {
    private static final Logger logger = LoggerFactory.getLogger(WeiboAshiService.class);
    private final HttpBuilder httpBuilder;

    private static final String INFO_URL = "https://weibo.com/login.php";
    private static final String UPDATE_URL = "https://account.weibo.com/set/aj5/photo/uploadv6?cb=https%3A%2F%2Fweibo.com%2Faj%2Fstatic%2Fupimgback.html%3F_wv%3D5%26callback%3DSTK_ijax_159905661747962";
    private static final String PROP_REGEX = "(?<=\\$CONFIG\\['%s']=').+?(?=')";

    @Autowired
    public WeiboAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        var html = httpBuilder.builder().url(INFO_URL).cookies(cookies).start(String.class);
        var nickName = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "nick"));
        if (StringUtils.isEmpty(nickName)) {
            throw new CookieExpireException();
        }
        var headImage = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "avatar_large"));
        return new AshiData(nickName, headImage);
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        byte[] fileContent;
        BufferedImage imageFile;
        try {
            fileContent = Files.readAllBytes(file.toPath());
            imageFile = ImageIO.read(file);
        } catch (IOException e) {
            throw new AshiException("file error");
        }
        var html = httpBuilder.builder().url(INFO_URL).cookies(cookies).start(String.class);
        var id = HttpUtils.RexHtml(html, String.format(PROP_REGEX, "uid"));
        String fileData = Base64.getEncoder().encodeToString(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Referer", "https://weibo.com/" + id + "/profile?rightmod=1&wvr=6&mod=personinfo&is_all=1");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("Filedata", fileData);
        body.add("ax", "0");
        body.add("ay", "0");
        body.add("aw", imageFile.getWidth());
        body.add("type", "jpeg");
        body.add("file_source", "5");
        var res = httpBuilder.builder().url(UPDATE_URL).method(HttpMethod.POST)
                .Headers(headers).body(body).cookies(cookies)
                .start(String.class);
        logger.info("weibo up html:" + res);
    }

    @Override
    public AshiType getType() {
        return AshiType.weibo;
    }
}
