package com.github.wensimin.ashioarae.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.TarCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * http工具类
 */
public class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static final SimpleClientHttpRequestFactory proxyFactory;
    // fixme hard code
    private static final String host = "192.168.0.201";
    private static final int port = 1080;
    private static final SimpleClientHttpRequestFactory requestFactory;
    private static final CookieErrorHandler errorhandler;

    static {
        errorhandler = new CookieErrorHandler();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        proxyFactory = new SimpleClientHttpRequestFactory();
        proxyFactory.setProxy(proxy);
        proxyFactory.setConnectTimeout(5000);
        proxyFactory.setReadTimeout(5000);
        requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
    }


    /**
     * 创建restTemplate
     *
     * @param proxy 是否使用代理
     * @return restTemplate
     */
    private static RestTemplate buildRestTemplate(boolean proxy) {
        RestTemplate restTemplate;
        if (proxy) {
            restTemplate = new RestTemplate(proxyFactory);
        } else {
            restTemplate = new RestTemplate(requestFactory);
        }
        restTemplate.setErrorHandler(errorhandler);
        return restTemplate;
    }

    /**
     * 将string cookie转化为map
     *
     * @param cookies cookie
     * @return map
     */
    public static Map<String, String> cookie2map(String cookies) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(cookies)) {
            return map;
        }
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
     *               * @param headers header
     * @param type   返回值类型
     * @return entity
     */
    public static <T> T get(String url, HttpHeaders headers, String cookie, Class<T> type, boolean proxy) {
        RestTemplate restTemplate = buildRestTemplate(proxy);
        headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        headers.add("cookie", cookie);
        var request = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, request, type);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AshiException("error:" + response.getStatusCode());
        }
        return response.getBody();
    }

    public static <T> T get(String url, List<TarCookie> cookies, Class<T> type, boolean proxy) {
        return get(url, new HttpHeaders(), cookies, type, proxy);
    }


    public static <T> T get(String url, List<TarCookie> cookies, Class<T> type) {
        return get(url, cookies, type, false);
    }

    public static <T> T get(String url, HttpHeaders headers, List<TarCookie> cookies, Class<T> type, boolean proxy) {
        String cookieString = cookie2String(getCookieByUrl(cookies, url));
        return get(url, headers, cookieString, type, proxy);
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
        RestTemplate restTemplate = buildRestTemplate(proxy);
        headers.add("cookie", cookie);
        headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        ResponseEntity<T> response = restTemplate
                .postForEntity(url, requestEntity, type);
        return response.getBody();
    }


    public static <T> T post(String url, MultiValueMap<String, Object> body, List<TarCookie> cookies, Class<T> type, boolean proxy) {
        HttpHeaders headers = new HttpHeaders();
        return post(url, headers, body, cookies, type, proxy);
    }


    public static <T> T post(String url, MultiValueMap<String, Object> body, List<TarCookie> cookies, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        return post(url, headers, body, cookies, type, false);
    }


    public static <T> T post(String url, HttpHeaders headers, MultiValueMap<String, Object> body, List<TarCookie> cookies, Class<T> type) {
        return post(url, headers, body, cookies, type, false);
    }


    public static <T> T post(String url, HttpHeaders headers, List<TarCookie> cookies, Class<T> type, boolean proxy) {
        return post(url, headers, new LinkedMultiValueMap<>(), cookies, type, proxy);
    }


    public static <T> T post(String url, HttpHeaders headers, List<TarCookie> cookies, Class<T> type) {
        return post(url, headers, cookies, type, false);
    }

    public static <T> T post(String url, HttpHeaders headers, MultiValueMap<String, Object> body, List<TarCookie> cookies, Class<T> type, boolean proxy) {
        String cookieString = cookie2String(getCookieByUrl(cookies, url));
        return post(url, headers, body, cookieString, type, proxy);
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

    /**
     * 通过正则获取html中的值
     *
     * @param html  html
     * @param regex regex
     * @return 捕获的组
     */
    public static String RexHtml(String html, String regex) {
        return Optional.of(Pattern.compile(regex)).
                map(p -> p.matcher(html))
                .filter(Matcher::find)
                .map(Matcher::group)
                .orElse(null);
    }


    private static class CookieErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CookieExpireException("cookie expire");
            } else {
                logger.error(StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
                throw new AshiException("error: " + response.getStatusCode());
            }
        }
    }

    /**
     * 查询cookie中指定name的第一个值
     *
     * @param cookies cookies
     * @param name    name
     * @return 第一个name对应
     */
    public static TarCookie getAttrInCookie(List<TarCookie> cookies, String name) {
        return cookies.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * 获取url下能使用的cookie
     *
     * @param cookies cookies
     * @param url     url
     * @return cookies
     */
    public static List<TarCookie> getCookieByUrl(List<TarCookie> cookies, String url) {
        return cookies.stream().filter(c -> {
            var domain = c.getDomain();
            // 以.开头的cookie domain 需要匹配本身
            if (domain.charAt(0) == '.') {
                domain = domain.substring(1);
            }
            return url.contains(domain) && url.contains(c.getPath());
        }).collect(Collectors.toList());
    }

    /**
     * 将cookie转化成String
     *
     * @param cookies cookie
     * @return string
     */
    public static String cookie2String(List<TarCookie> cookies) {
        StringBuilder cookieString = new StringBuilder();
        for (var c : cookies) {
            cookieString.append(c.getName()).append("=").append(c.getValue()).append(";");
        }
        return cookieString.toString();
    }

}
