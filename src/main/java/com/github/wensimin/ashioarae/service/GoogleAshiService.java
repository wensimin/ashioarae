package com.github.wensimin.ashioarae.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * google service
 * google的api相对非常老旧,此service使用了部分hack实现,有效性存疑
 * 一共进行4部分上传
 * 第一步上传参数预告
 * 第二步进行文件上传
 * 第三步将第二部生成的临时文件copy
 * 最后选择copy后的文件id
 * 其次,google的各平台同步头像机制比较奇怪,缓存也比较多.待观察此头像修改后的同步情况
 */
@Service
public class GoogleAshiService implements AshioaraeInterface {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAshiService.class);
    private final HttpBuilder httpBuilder;
    private static final String INFO_URL = "https://myaccount.google.com/u/0/personal-info";
    private static final String PICK_URL = "https://docs.google.com/picker";
    private static final String UPLOAD_URL_1 = "https://docs.google.com/upload/photos/resumable?authuser=0";
    private static final String UPLOAD_URL_2 = "https://docs.google.com/upload/photos/resumable?authuser=0&upload_id=%s&file_id=000";
    private static final String UPLOAD_URL_3 = "https://docs.google.com/picker/mutate?origin=https%3A%2F%2Fmyaccount.google.com&hostId=ac";


    private static final String NICK_REGEX = "(?<=<div class=\"gb_ub\">).+?(?=<\\/div>)";
    private static final String HEAD_REGEX = "(?<=<img class=\"gb_La gbii\" src=\").+?(?=\")";
    // attr正则,一个小hack 使用','开头来分开x-token与token,但是无法抓取到第一个值,不过似乎在这里没有影响
    private static final String ATTR_REGEX = "(?<=,%s:').+?(?=')";

    @Autowired
    public GoogleAshiService(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
    }

    @Override
    public AshiData getInfo(List<TarCookie> cookies) {
        String html = httpBuilder.builder().url(INFO_URL).cookies(cookies).proxy().start(String.class);
        String nickname = HttpUtils.RexHtml(html, NICK_REGEX);
        String headImage = HttpUtils.RexHtml(html, HEAD_REGEX);
        if (StringUtils.isEmpty(nickname)) {
            throw new CookieExpireException();
        }
        headImage = headImage.replace("s32", "s400");
        return new AshiData(nickname, headImage);
    }

    @Override
    public void updateHeadImage(List<TarCookie> cookies, File file) {
        String pickHtml = httpBuilder.builder().url(PICK_URL).cookies(cookies).proxy().start(String.class);
        var token = HttpUtils.RexHtml(pickHtml, String.format(ATTR_REGEX, "token"));
        var xToken = HttpUtils.RexHtml(pickHtml, String.format(ATTR_REGEX, "xtoken"));
        var userId = HttpUtils.RexHtml(pickHtml, String.format(ATTR_REGEX, "userId"));
        var clientUser = HttpUtils.RexHtml(pickHtml, String.format(ATTR_REGEX, "clientUser"));
        var uploadId = this.upload1(cookies, file, userId);
        // 上传获取临时文件id
        var photoId = this.upload2(cookies, file, uploadId);
        // 进行复制临时文件
        var copyPhotoId = this.uploadCopy(cookies, photoId, token, xToken, clientUser);
        this.upload3(cookies, copyPhotoId, token, xToken, clientUser);
    }

    private String uploadCopy(List<TarCookie> cookies, String photoId, String token, String xToken, String clientUser) {
        var url = UPLOAD_URL_3 + "&xtoken=" + xToken;
        var body = "ids=%5B%22picasa.0." + photoId + "%22%5D" +
                "&service=picasa" +
                "&operation=copy" +
                "&options=%7B%22label%22%3A%22profile_photos.active%22%2C%22preventDuplicates%22%3A%22true%22%7D" +
                "&token=" + token +
                "&version=4&app=2" +
                "&clientUser=1" + clientUser +
                "&subapp=5&authuser=0";
        var res = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body).cookies(cookies)
                .proxy()
                .start(String.class);
        var startStr = "&&&START&&&";
        var resJson = res.substring(res.lastIndexOf(startStr) + startStr.length());
        JsonNode resNode;
        try {
            resNode = new ObjectMapper().readTree(resJson);
        } catch (JsonProcessingException e) {
            throw new AshiException("json error:" + e.getMessage());
        }
        return Optional.of(resNode)
                .map(r -> r.get("response"))
                .map(r -> r.get("docs"))
                .map(r -> r.get(0))
                .map(r -> r.get("id"))
                .map(JsonNode::asText).orElseThrow();
    }

    private void upload3(List<TarCookie> cookies, String photoId, String token, String xToken, String clientUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var url = UPLOAD_URL_3 + "&xtoken=" + xToken;
        var body = "ids=%5B%22picasa.0." + photoId + "%22%5D&" +
                "service=picasa&operation=setProfile&options=%7B%22top%22%3A0%2C%22right%22%3A1%2C%22bottom%22%3A1%2C%22left%22%3A0%7D&" +
                "token=" + token + "" +
                "&version=4&app=2&" +
                "clientUser=" + clientUser + "" +
                "&subapp=5&authuser=0";
        var res = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(url).headers(headers)
                .body(body).cookies(cookies)
                .proxy()
                .start(String.class);
        logger.info("google uploadHead final: " + res);
    }

    private String upload2(List<TarCookie> cookies, File file, String uploadId) {
        var url = String.format(UPLOAD_URL_2, uploadId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        var res = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(url).headers(headers)
                .body(new FileSystemResource(file)).cookies(cookies)
                .proxy().converter(new GoogleHttpMessageConverter())
                .start(GoogleResponse.class);
        checkRes(res);
        JsonNode additionalInfo = res.getSessionStatus().getAdditionalInfo();
        return Optional.of(additionalInfo)
                .map(j -> j.get("uploader_service.GoogleRupioAdditionalInfo"))
                .map(j -> j.get("completionInfo"))
                .map(j -> j.get("customerSpecificInfo"))
                .map(j -> j.get("photoid"))
                .map(JsonNode::asText)
                .orElse(null);
    }

    private String upload1(List<TarCookie> cookies, File file, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Calendar now = Calendar.getInstance();
        var albumName = now.get(Calendar.YEAR) + "年" + (now.get(Calendar.MONTH) + 1) + "月" + now.get(Calendar.DAY_OF_MONTH) + "日";
        var body = "{\n" +
                "  \"protocolVersion\": \"0.8\",\n" +
                "  \"createSessionRequest\": {\n" +
                "    \"fields\": [\n" +
                "      {\n" +
                "        \"external\": {\n" +
                "          \"name\": \"file\",\n" +
                "          \"filename\": \"" + file.getName() + "\",\n" +
                "          \"put\": {\n" +
                "            \n" +
                "          },\n" +
                "          \"size\": " + file.length() + "\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"use_upload_size_pref\",\n" +
                "          \"content\": \"true\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"async_thumbnail\",\n" +
                "          \"content\": \"false\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"album_mode\",\n" +
                "          \"content\": \"temporary\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"disable_asbe_notification\",\n" +
                "          \"content\": \"true\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"enable_face_detection\",\n" +
                "          \"content\": \"true\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"silo_id\",\n" +
                "          \"content\": \"49\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"title\",\n" +
                "          \"content\": \"" + file.getName() + "\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"addtime\",\n" +
                "          \"content\": \"+" + System.currentTimeMillis() + "+\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"c189022504\",\n" +
                "          \"content\": \"true\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"batchid\",\n" +
                "          \"content\": \"" + System.currentTimeMillis() + "\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"album_name\",\n" +
                "          \"content\": \"" + albumName + "\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"album_abs_position\",\n" +
                "          \"content\": \"0\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"effective_id\",\n" +
                "          \"content\": \"" + userId + "\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"inlined\": {\n" +
                "          \"name\": \"client\",\n" +
                "          \"content\": \"ac\",\n" +
                "          \"contentType\": \"text/plain\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        var res = httpBuilder.builder()
                .method(HttpMethod.POST)
                .url(UPLOAD_URL_1).headers(headers)
                .body(body).cookies(cookies)
                .proxy().converter(new GoogleHttpMessageConverter())
                .start(GoogleResponse.class);
        checkRes(res);
        return res.getSessionStatus().getUploadId();
    }

    private void checkRes(GoogleResponse res) {
        if (!StringUtils.isEmpty(res.getErrorMessage())) {
            throw new AshiException(res.getErrorMessage());
        }
    }

    @Override
    public AshiType getType() {
        return AshiType.google;
    }

    private static class GoogleResponse {
        private SessionStatus sessionStatus;
        private String errorMessage;

        public SessionStatus getSessionStatus() {
            return sessionStatus;
        }

        public void setSessionStatus(SessionStatus sessionStatus) {
            this.sessionStatus = sessionStatus;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        private static class SessionStatus {
            private String state;
            @JsonAlias("upload_id")
            private String uploadId;
            private JsonNode additionalInfo;

            public JsonNode getAdditionalInfo() {
                return additionalInfo;
            }

            public void setAdditionalInfo(JsonNode additionalInfo) {
                this.additionalInfo = additionalInfo;
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getUploadId() {
                return uploadId;
            }

            public void setUploadId(String uploadId) {
                this.uploadId = uploadId;
            }
        }
    }

    private static class GoogleHttpMessageConverter extends MappingJackson2HttpMessageConverter {
        @Override
        public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
            supportedMediaTypes = new ArrayList<>();
            supportedMediaTypes.add(MediaType.TEXT_HTML);
            super.setSupportedMediaTypes(supportedMediaTypes);
        }
    }

}
