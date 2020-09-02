package com.github.wensimin.ashioarae.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wensimin.ashioarae.entity.TarCookie;
import org.springframework.util.StringUtils;

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
