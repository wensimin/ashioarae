package com.github.wensimin.ashioarae;

import com.github.wensimin.ashioarae.service.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;

@SpringBootTest
class AshioaraeApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        var url = "https://steamcommunity.com/actions/FileUploader/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        File file = new File("file/shali/preHead.jpg");
        body.add("avatar", new FileSystemResource(file));
        body.add("type", "player_avatar_image");
        body.add("sId", "76561198086788011");
        body.add("sessionid", "a87fcc44f2203d8db8e14628");
        body.add("doSub", 1);
        body.add("json", 1);
        String cookie = "steamMachineAuth76561198086788011=CDE68C6DC4B1463452229B4958EC193EDEA9F5ED; timezoneOffset=28800,0; _ga=GA1.2.555098967.1514381243; steamMachineAuth76561198102303458=49CA4B31A4B92B6B5090F2352DA01190E1059DBD; webTradeEligibility=%7B%22allowed%22%3A1%2C%22allowed_at_time%22%3A0%2C%22steamguard_required_days%22%3A15%2C%22new_device_cooldown_days%22%3A7%2C%22time_checked%22%3A1554738383%7D; browserid=1416887218352216889; recentlyVisitedAppHubs=322330%2C570%2C230410%2C504210%2C964350%2C620%2C365450%2C583950%2C730%2C374320%2C353370%2C431960%2C814380%2C353380%2C271590%2C50620%2C361420%2C346110%2C252490; app_impressions=322330@2_100300_300_|431960@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|814380@2_9_100006_100202|431960@2_100100_100101_100103|431960@2_100100_100101_100103|431960@2_100100_100101_100103|582010@2_groupannouncements_detail_|353380@2_9_100006_|353380@2_9_100006_100202|353380@2_9_100006_|620@2_100100_100101_100103|271590@2_9_100006_100202|698780@2_100100_100101_100106|698780@2_100100_100101_100106|50620@2_9_100006_100202|252490@2_9_100015_100202|252490@2_9_100006_100202|252490@2_9_100006_100202|361420@2_9_100010_|322330@2_100100_100101_100103|322330@2_100100_100101_100103|322330@2_100100_100101_100103|346110@2_9_100006_100202|252490@2_9_100006_100202|252490@2_9_100006_100202|346110@2_100100_100101_100105|346110@2_100100_230_|431960@2_100100_100101_100103|1097150@2_100400_100401_; steamCountry=HK%7Cbc19c8dc58720937b2e167dc6c54f66f; _gid=GA1.2.1381853043.1598011431; steamLoginSecure=76561198086788011%7C%7C314BB5C7B83553087B01FA9695381F34100F059D; steamRememberLogin=76561198086788011%7C%7Ceb17e699e7bf0cd71bb747145035b49e; sessionid=a87fcc44f2203d8db8e14628";
        headers.add("cookie", cookie);
        headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.201", 1080));
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        var response = restTemplate.postForEntity(url, requestEntity, String.class);
        var bodyS = response.getBody();
        System.out.println(bodyS);
    }
}
