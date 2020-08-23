package com.github.wensimin.ashioarae.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * http工具类
 */
public class HttpUtils {
    private static final SimpleClientHttpRequestFactory requestFactory;

    static {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080));
        requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
    }


    /**
     * 将string cookie转化为map
     *
     * @param cookies cookie
     * @return map
     */
    public static Map<String, String> cookie2map(String cookies) {
        Map<String, String> map = new HashMap<>();
        String[] cookiesArray = cookies.split(";");
        for (String c : cookiesArray) {
            String[] cookie = c.split("=");
            map.put(cookie[0].trim(), cookie[1].trim());
        }
        return map;
    }

    /**
     * 带cookie发起get
     *
     * @param <T>    返回值类型
     * @param url    目标url
     * @param cookie cookie
     * @param type   返回值类型
     * @return entity
     */
    public static <T> T get(String url, String cookie, Class<T> type, boolean proxy) {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate;
        if (proxy) {
            restTemplate = new RestTemplate(requestFactory);
        } else {
            restTemplate = new RestTemplate();
        }
        headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        headers.add("cookie", cookie);
        var request = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, request, type);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AshiException("error:" + response.getStatusCode());
        }
        return response.getBody();
    }

    public static <T> T get(String url, String cookie, Class<T> type) {
        return get(url, cookie, type, false);
    }


    /**
     * post 请求
     *
     * @param url     url
     * @param headers header
     * @param body    body
     * @param cookie  cookie
     * @param type    type
     * @param <T>     type
     * @return body
     */
    public static <T> T post(String url, HttpHeaders headers, MultiValueMap<String, Object> body, String cookie, Class<T> type, boolean proxy) {
        RestTemplate restTemplate;
        if (proxy) {
            restTemplate = new RestTemplate(requestFactory);
        } else {
            restTemplate = new RestTemplate();
        }
        headers.add("cookie", cookie);
        headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        ResponseEntity<T> response = restTemplate
                .postForEntity(url, requestEntity, type);
        return response.getBody();
    }

    public static <T> T post(String url, MultiValueMap<String, Object> body, String cookie, Class<T> type, boolean proxy) {
        HttpHeaders headers = new HttpHeaders();
        return post(url, headers, body, cookie, type, proxy);
    }

    public static <T> T post(String url, MultiValueMap<String, Object> body, String cookie, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        return post(url, headers, body, cookie, type, false);
    }

    public static <T> T post(String url, HttpHeaders headers, MultiValueMap<String, Object> body, String cookie, Class<T> type) {
        return post(url, headers, body, cookie, type, false);
    }


    /**
     * string json 2 map
     *
     * @param json json
     * @return map
     */
    public static Map<String, String> json2Map(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
