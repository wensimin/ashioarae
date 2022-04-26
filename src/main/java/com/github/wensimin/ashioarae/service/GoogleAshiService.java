package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * google service
 * update 2022年4月26日
 * google重构了修改头像的api
 * 跟进修改,分为预上传-上传-选择头像三步
 * 其次,google的各平台同步头像机制比较奇怪,缓存也比较多.待观察此头像修改后的同步情况
 */
@Service
public class GoogleAshiService implements AshioaraeInterface {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAshiService.class);
    private final HttpBuilder httpBuilder;
    private static final String INFO_URL = "https://myaccount.google.com/u/0/personal-info";


    // 目前只支持中文 2022年4月26日 修改为正则html获取且暂支持中文
    private static final String INFO_REGEX = "(?<=Google 帐号： ).*?(?= )";
    private static final String HEAD_REGEX = "(?<=src=\")https:\\/\\/lh3\\.googleusercontent\\.com\\/ogw\\/.+?(?=\")";

    @Autowired
    public GoogleAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        String html = httpBuilder.builder().url(INFO_URL).cookies(cookies).proxy().run(String.class);
        String headImage = HttpUtils.RexHtml(html, HEAD_REGEX).replace("s32", "s1024");
        String nickname = HttpUtils.RexHtml(html, INFO_REGEX);
        if (StringUtils.isEmpty(nickname)) {
            throw new CookieExpireException();
        }
        return new AshiData(nickname, headImage);
    }

    //FIXME 已无效
    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        var uploadId = startUpload(cookies, file);
        var uploadRes = upload(uploadId, cookies, file);
        selectHead(uploadRes.get("media_key").textValue(), cookies);
    }

    private void selectHead(String id, List<TarCookie> cookies) {
        var url = "https://myaccount.google.com/_/ProfilePicturePickerUi/data/batchexecute?" +
                "rpcids=wcu88&source-path=%2Fprofile-picture%2Fcrop%2F" + id + "&" +
                "f.sid=-8840432055937876258&bl=boq_identityaccountsettingsuiserver_20220419.06_p0&hl=zh-CN&soc-app=1&" +
                "soc-platform=1&soc-device=1&_reqid=132169&rt=c";
        var body = "f.req=%5B%5B%5B%22wcu88%22%2C%22%5B%5B%5B5%2C3%2C%5C%22" +
                "boq_identityaccountsettingsuiserver_20220419.06_p0%5C%22%2C64%5D%5D%2C%5C%22" +
                id +
                "%5C%22%2C%5B1%5D%2C%5B0%2C0%2C1%2C1%5D%2C%5Bnull%2C%5B%5D%5D%2Cfalse%5D%22" +
                "%2Cnull%2C%22generic%22%5D%5D%5D&at=AJ4vme4pVJ46Az94Szg4iKo0FkHA%3A1650934565981&";
        logger.debug(initHeaders(httpBuilder.builder())
                .method(HttpMethod.POST)
                .url(url)
                .proxy()
                .body(body)
                .cookies(cookies)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .run(String.class));
    }

    private JsonNode upload(String uploadId, List<TarCookie> cookies, File file) {
        return initHeaders(httpBuilder
                .builder())
                .method(HttpMethod.POST)
                .header("x-goog-upload-command", "upload, finalize")
                .header("x-goog-upload-offset", "0")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new FileSystemResource(file))
                .url("https://myaccount.google.com/_/profilepicturepicker/upload?upload_id=" + uploadId + "&upload_protocol=resumable")
                .cookies(cookies).proxy().run(JsonNode.class);
    }

    private HttpBuilder.Http initHeaders(HttpBuilder.Http http) {
        return http.header("authority", "myaccount.google.com")
                .header("accept", "*/*")
                .header("accept-language", "zh-CN,zh;q=0.9,zh-TW;q=0.8,ja;q=0.7,en;q=0.6")
                .header("origin", "https://myaccount.google.com")
                .header("referer", "https://myaccount.google.com/")
                .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"")
                .header("sec-ch-ua-arch", "\"x86\"")
                .header("sec-ch-ua-bitness", "\"64\"")
                .header("sec-ch-ua-full-version", "\"100.0.4896.127\"")
                .header("sec-ch-ua-full-version-list", "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-model", "")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-ch-ua-platform-version", "\"10.0.0\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36")
                .header("x-client-data", "CKS1yQEIhbbJAQiitskBCMG2yQEIqZ3KAQiE98oBCJOhywEImKHLAQie+csBCOaEzAEIpY7MAQibmswBCNCizAEIg6fMAQikqcwBGKupygE=");
    }

    private String startUpload(List<TarCookie> cookies, File file) {
        ResponseEntity<Void> startUpload = initHeaders(httpBuilder
                .builder())
                .method(HttpMethod.POST)
                .header("x-goog-upload-command", "start")
                .header("x-goog-upload-header-content-length", String.valueOf(file.length()))
                .header("x-goog-upload-protocol", "resumable")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("File name: " + file.getName())
                .url("https://myaccount.google.com/_/profilepicturepicker/upload")
                .cookies(cookies).proxy().runResponse(Void.class);
        return startUpload.getHeaders().getFirst("X-GUploader-UploadID");
    }


    @Override
    public AshiType getType() {
        return AshiType.google;
    }



}
